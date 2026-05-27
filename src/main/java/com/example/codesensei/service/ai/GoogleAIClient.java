package com.example.codesensei.service.ai;

import com.example.codesensei.model.AiReview;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleAIClient {

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

            Map<?, ?> response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<?> candidates = (List<?>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No AI candidates returned");
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);

            Map<?, ?> content = (Map<?, ?>) candidate.get("content");

            List<?> parts = (List<?>) content.get("parts");

            Map<?, ?> part = (Map<?, ?>) parts.get(0);

            String json = part.get("text").toString();

            json = json.replace("```json", "")
                    .replace("```", "")
                    .trim();

            return mapper.readValue(json, AiReview.class);

        } catch (Exception e) {

            AiReview error = new AiReview();

            error.setScore(0);

            error.setSummary("AI analysis failed: " + e.getMessage());

            return error;
        }
    }
}