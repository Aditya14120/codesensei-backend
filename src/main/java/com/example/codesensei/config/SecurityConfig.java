package com.example.codesensei.config;

import com.example.codesensei.exception.ApiErrorResponse;
import com.example.codesensei.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint(this::handleUnauthorized)
                    .accessDeniedHandler(this::handleForbidden)
            )
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Called by Spring Security (before the DispatcherServlet) when a protected route is hit with no/invalid token. */
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException) throws java.io.IOException {
        writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                "Authentication required, or the token is missing/invalid/expired");
    }

    /** Called when an authenticated user is missing the role/authority required for the route. */
    private void handleForbidden(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException accessDeniedException) throws java.io.IOException {
        writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                "You do not have permission to perform this action");
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, int status, String error,
            String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(), status, error, message, request.getRequestURI(), null);
        objectMapper.writeValue(response.getWriter(), body);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
