package com.example.codesensei.service.ai;

import com.example.codesensei.model.AiReview;
import com.example.codesensei.model.SupportedLanguage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
public class GroqAIClient {

    private static final Logger log = LoggerFactory.getLogger(GroqAIClient.class);

    private static final String MODEL = "llama-3.3-70b-versatile";

    private final WebClient webClient;

    private final ObjectMapper mapper = new ObjectMapper();

    public GroqAIClient(@Value("${groq.api.key}") String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public AiReview getCodeReview(String code) {
        return getCodeReview(code, SupportedLanguage.JAVA);
    }

    public AiReview getCodeReview(String code, SupportedLanguage language) {

        String languageName = language.getDisplayName();

        String prompt = """
You are a senior Google-level software engineer and code reviewer.

Analyze the provided %s code deeply and honestly.

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

  "improvedCode": "full improved %s code",

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
""".formatted(languageName, languageName) + code;

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt)),
                "temperature", 0.2);

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

            log.warn("Groq analysis failed: {}", e.getMessage());
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
        JsonNode textNode = root.path("choices")
                .path(0)
                .path("message")
                .path("content");

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
