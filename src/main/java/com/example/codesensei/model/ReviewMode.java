package com.example.codesensei.model;

import java.util.Locale;

/**
 * Controls the tone of the AI review prompt. Both modes return the exact
 * same JSON schema (via {@link AiReview}) — only the persona instructions
 * sent to the model differ, so the rest of the pipeline (scoring, parsing,
 * persistence) doesn't need to know which mode ran.
 */
public enum ReviewMode {

    PROFESSIONAL,
    ROAST;

    public static ReviewMode fromId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return PROFESSIONAL;
        }

        try {
            return ReviewMode.valueOf(rawId.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return PROFESSIONAL;
        }
    }
}
