package com.example.codesensei.service.ai;

import com.example.codesensei.model.AiReview;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class GoogleAIClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleAIClient.class);

    private final WebClient webClient;

    private final ObjectMapper mapper = new ObjectMapper();

    public GoogleAIClient(@Value("${gemini.api.key}") String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                                + apiKey)
                .build();
    }

    public AiReview getCodeReview(String code) {

        String prompt = """
You are a senior Google-level software engineer and code reviewer.

Analyze the provided Java code deeply and honestly.

RETURN ONLY VALID JSON.

JSON FORMAT:

{
  "score": 0,
  "summary": "short technical explanation",

  "bugs": [
    "bug 1"
  ],

  "codeSmells": [
    "issue 1"
  ],

  "performanceIssues": [
    "issue 1"
  ],

  "securityIssues": [
    "issue 1"
  ],

  "improvements": [
    "improvement 1"
  ],

  "improvedCode": "full improved Java code",

  "learningTips": [
    "tip 1"
  ]
}

RULES:
- No markdown
- No ```json
- No extra explanation
- Strict valid JSON only
- Score must depend on code quality
- Bad code -> low score
- Good code -> high score

CODE:
""" + code;

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)))));

        try {

            String response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            String json = extractJson(extractCandidateText(response));

            return withDefaults(mapper.readValue(json, AiReview.class), code);

        } catch (Exception e) {

            log.warn("Gemini analysis failed: {}", e.getMessage());
            return fallbackReview(code, "AI analysis failed: " + e.getMessage());
        }
    }

    public String getCodeExplanation(String code) {
        return getCodeReview(code).getSummary();
    }

    private String extractCandidateText(String responseBody) throws Exception {

        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new IllegalStateException("Empty AI response");
        }

        JsonNode root = mapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().trim().isEmpty()) {
            throw new IllegalStateException("No AI candidate text returned");
        }

        return textNode.asText();
    }

    private String extractJson(String text) {

        String cleaned = text.replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalStateException("AI response did not contain a JSON object");
        }

        return cleaned.substring(start, end + 1);
    }

    private AiReview withDefaults(AiReview review, String code) {

        AiReview safe = review != null ? review : new AiReview();

        safe.setScore(Math.max(0, Math.min(10, safe.getScore())));
        safe.setSummary(defaultString(safe.getSummary(), "AI analysis completed."));
        safe.setBugs(defaultList(safe.getBugs()));
        safe.setCodeSmells(defaultList(safe.getCodeSmells()));
        safe.setPerformanceIssues(defaultList(safe.getPerformanceIssues()));
        safe.setSecurityIssues(defaultList(safe.getSecurityIssues()));
        safe.setImprovements(defaultList(safe.getImprovements()));
        safe.setImprovedCode(defaultString(safe.getImprovedCode(), code));
        safe.setLearningTips(defaultList(safe.getLearningTips()));

        return safe;
    }

    private AiReview fallbackReview(String code, String summary) {

        AiReview fallback = new AiReview();

        fallback.setScore(0);
        fallback.setSummary(summary);
        fallback.setBugs(Collections.emptyList());
        fallback.setCodeSmells(Collections.emptyList());
        fallback.setPerformanceIssues(Collections.emptyList());
        fallback.setSecurityIssues(Collections.emptyList());
        fallback.setImprovements(Collections.emptyList());
        fallback.setImprovedCode(code);
        fallback.setLearningTips(Collections.emptyList());

        return fallback;
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
