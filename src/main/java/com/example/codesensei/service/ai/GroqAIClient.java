package com.example.codesensei.service.ai;

import com.example.codesensei.model.AiReview;
import com.example.codesensei.model.ReviewMode;
import com.example.codesensei.model.SupportedLanguage;
import com.example.codesensei.model.TranslationResult;
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

    private static final String PROFESSIONAL_PERSONA = """
            You are a senior Google-level software engineer and code reviewer.

            Analyze the provided %s code deeply and honestly. Be precise, constructive, and professional.""";

    private static final String ROAST_PERSONA = """
            You are a senior software engineer doing a comedic "roast" code review — a stand-up
            comedian who happens to be a 10x engineer. Analyze the provided %s code deeply and
            honestly, but write every finding (summary, bugs, codeSmells, performanceIssues,
            securityIssues, improvements) with sharp, sarcastic humor.

            ROAST RULES:
            - Roast the CODE and its choices, never the coder as a person — no insults about
              intelligence, background, or identity.
            - No profanity, slurs, or genuinely mean-spirited language. Clever, not cruel — like a
              friendly roast between senior engineers, not bullying.
            - Every joke must be built on a real, technically accurate observation. Humor never
              replaces substance.
            - "improvedCode" must still be a completely serious, correct, production-ready rewrite —
              save the jokes for the commentary fields, not the code itself.""";

    private final WebClient webClient;

    private final ObjectMapper mapper = new ObjectMapper();

    public GroqAIClient(@Value("${groq.api.key}") String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public AiReview getCodeReview(String code) {
        return getCodeReview(code, SupportedLanguage.JAVA, ReviewMode.PROFESSIONAL);
    }

    public AiReview getCodeReview(String code, SupportedLanguage language) {
        return getCodeReview(code, language, ReviewMode.PROFESSIONAL);
    }

    public AiReview getCodeReview(String code, SupportedLanguage language, ReviewMode mode) {

        String languageName = language.getDisplayName();
        String prompt = buildPrompt(code, languageName, mode);

        // Roast mode gets a slightly higher temperature so the humor doesn't read
        // identically every time; professional mode stays deterministic-ish for consistency.
        double temperature = mode == ReviewMode.ROAST ? 0.6 : 0.2;

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt)),
                "temperature", temperature);

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

    /**
     * Translates code between supported languages via the same request/response
     * plumbing used for reviews, but with its own minimal prompt and JSON shape
     * (no score, no issue lists — just the translated code).
     */
    public String translateCode(String code, SupportedLanguage source, SupportedLanguage target) {

        String prompt = """
                You are an expert software engineer fluent in Java, Python, C, and C++.

                Translate the following %s code into idiomatic, correct, production-ready %s code.
                Preserve the program's behavior and I/O exactly. Use the target language's standard
                library and conventions rather than a literal line-by-line port.

                RETURN ONLY VALID JSON.

                JSON FORMAT:

                {
                  "translatedCode": "full translated %s code as a single string"
                }

                RULES:
                - No markdown, no ```json
                - No extra explanation
                - Strict valid JSON only
                - Preserve behavior exactly — this must compile/run and do the same thing as the source
                - "translatedCode" MUST be properly formatted, multi-line, indented code — use actual
                  "\\n" newline escapes between lines and consistent indentation, exactly like a real
                  source file. NEVER return it as one flattened line of code.

                SOURCE (%s):
                """.formatted(source.getDisplayName(), target.getDisplayName(), target.getDisplayName(), source.getDisplayName())
                + code;

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
            TranslationResult result = mapper.readValue(json, TranslationResult.class);

            if (result.getTranslatedCode() == null || result.getTranslatedCode().trim().isEmpty()) {
                throw new IllegalStateException("AI returned an empty translation");
            }

            return result.getTranslatedCode();

        } catch (Exception e) {
            log.warn("Groq translation failed: {}", e.getMessage());
            throw new IllegalStateException("Translation failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String code, String languageName, ReviewMode mode) {

        String persona = (mode == ReviewMode.ROAST ? ROAST_PERSONA : PROFESSIONAL_PERSONA)
                .formatted(languageName);

        String schema = """

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
                - Score must depend on code quality, not tone — an honest quality score even in roast mode
                - Bad code -> low score
                - Good code -> high score
                - "improvedCode" MUST be properly formatted, multi-line, indented %s code —
                  use actual "\\n" newline escapes between lines and consistent indentation, exactly
                  like a real source file. NEVER return it as one flattened line of code.

                CODE:
                """.formatted(languageName, languageName);

        return persona + schema + code;
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
