package com.example.codesensei.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleAIClient {

    private final WebClient webClient;

    public GoogleAIClient(@Value("${gemini.api.key}") String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                                + apiKey
                )
                .build();
    }

    public String getCodeExplanation(String code) {

        String prompt = """
You are a senior Google-level software engineer and code reviewer.

Analyze the provided Java code deeply and honestly.

Your review MUST depend entirely on:
- logic correctness
- code quality
- readability
- optimization
- maintainability
- edge case handling
- security
- clean coding practices

DO NOT give generic reviews.
DO NOT give fixed scores.
DO NOT praise unnecessarily.

A bad code must receive a low score.
A clean optimized code must receive a high score.

--------------------------------------------------

RETURN RESPONSE ONLY IN VALID JSON.

DO NOT WRITE MARKDOWN.
DO NOT USE ```json
DO NOT ADD EXTRA TEXT.

JSON FORMAT:

{
  "summary": "short technical explanation of what the code does",

  "score": 0.0,

  "improvements": [
    "specific improvement 1",
    "specific improvement 2",
    "specific improvement 3"
  ],

  "improvedCode": "full improved Java code"
}

--------------------------------------------------

SCORING RULES:

9-10:
- production-quality
- optimized
- clean architecture
- excellent naming
- proper edge-case handling

7-8:
- good logic
- minor issues
- decent readability

5-6:
- average code
- multiple improvements needed
- moderate bad practices

3-4:
- poor structure
- weak readability
- inefficient logic
- risky coding practices

0-2:
- broken logic
- dangerous code
- very poor quality

--------------------------------------------------

STRICT REVIEW CRITERIA:

Check for:
- bad variable names
- repeated code
- unused variables
- poor formatting
- null safety
- nested complexity
- inefficient loops
- unnecessary objects
- bad OOP practices
- memory inefficiency
- exception handling
- scalability issues
- security problems
- lack of modularity

--------------------------------------------------

IMPORTANT:
- Score MUST vary according to code quality
- Improvements MUST be code-specific
- improvedCode MUST actually improve the original code
- Response MUST be STRICT VALID JSON ONLY

--------------------------------------------------

CODE TO REVIEW:
""" + code;

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {

            Map<String, Object> response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return """
                {
                  "summary": "No response from AI",
                  "score": 0,
                  "improvements": [],
                  "improvedCode": ""
                }
                """;
            }

            List<?> candidates = (List<?>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return """
                {
                  "summary": "No candidates returned by AI",
                  "score": 0,
                  "improvements": [],
                  "improvedCode": ""
                }
                """;
            }

            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);

            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");

            if (content == null) {
                return """
                {
                  "summary": "No content returned by AI",
                  "score": 0,
                  "improvements": [],
                  "improvedCode": ""
                }
                """;
            }

            List<?> parts = (List<?>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                return """
                {
                  "summary": "No parts returned by AI",
                  "score": 0,
                  "improvements": [],
                  "improvedCode": ""
                }
                """;
            }

            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);

            Object text = firstPart.get("text");

            if (text == null) {
                return """
                {
                  "summary": "AI returned empty text",
                  "score": 0,
                  "improvements": [],
                  "improvedCode": ""
                }
                """;
            }

            return text.toString();

        } catch (Exception e) {

            return """
            {
              "summary": "AI analysis failed",
              "score": 0,
              "improvements": ["%s"],
              "improvedCode": ""
            }
            """.formatted(e.getMessage());
        }
    }
}