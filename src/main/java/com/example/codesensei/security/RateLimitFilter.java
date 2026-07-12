package com.example.codesensei.security;

import com.example.codesensei.exception.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Per-user request throttling on the endpoints that call Groq — each analyze/translate call is a
 * paid third-party API request, so this is cost/abuse protection, not just traffic shaping.
 * Runs after {@link JwtAuthFilter} so the authenticated principal is already on the
 * SecurityContext; buckets are keyed by user id rather than IP since every limited route already
 * requires login. In-memory only — fine for a single instance, would need a shared store
 * (e.g. Redis) if this ever runs behind more than one backend instance.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of("/api/code/analyze", "/api/code/translate");

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final int capacity;
    private final int refillMinutes;

    public RateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.capacity:10}") int capacity,
            @Value("${app.rate-limit.refill-minutes:1}") int refillMinutes) {
        this.objectMapper = objectMapper;
        this.capacity = capacity;
        this.refillMinutes = refillMinutes;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String key = (auth != null && auth.isAuthenticated()) ? auth.getName() : request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(refillMinutes * 60));

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(), 429, "Too Many Requests",
                "You're sending requests too quickly. Please wait a moment and try again.",
                request.getRequestURI(), null);

        objectMapper.writeValue(response.getWriter(), body);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }
}
