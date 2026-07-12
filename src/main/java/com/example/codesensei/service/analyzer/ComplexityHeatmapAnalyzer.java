package com.example.codesensei.service.analyzer;

import com.example.codesensei.model.LineComplexity;
import com.example.codesensei.model.SupportedLanguage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristic per-line complexity scoring for the heatmap: nesting depth
 * (indentation for Python, brace balance for Java/C/C++) plus control-flow
 * keyword and boolean-operator density. Not a full parser/AST — a
 * lightweight, explainable signal, in the same spirit as this project's
 * other hand-rolled analyzers (PMD/Checkstyle/SpotBugs).
 */
@Component
public class ComplexityHeatmapAnalyzer {

    private static final Pattern CONTROL_FLOW = Pattern.compile(
            "\\b(if|else|for|while|switch|case|catch|do|elif|except)\\b");

    public List<LineComplexity> analyze(String code, SupportedLanguage language) {

        List<LineComplexity> result = new ArrayList<>();
        if (code == null || code.isBlank()) {
            return result;
        }

        String[] lines = code.split("\n", -1);
        boolean indentBased = language == SupportedLanguage.PYTHON;
        int braceDepth = 0;

        for (int i = 0; i < lines.length; i++) {
            String rawLine = lines[i];
            String trimmed = rawLine.trim();

            if (trimmed.isEmpty() || isCommentOnly(trimmed, language)) {
                continue;
            }

            int depth;
            if (indentBased) {
                depth = indentLevel(rawLine);
            } else {
                int depthBefore = braceDepth;
                long opens = trimmed.chars().filter(c -> c == '{').count();
                long closes = trimmed.chars().filter(c -> c == '}').count();
                braceDepth = Math.max(0, braceDepth + (int) (opens - closes));
                // The shallower of before/after avoids over-scoring a line that's
                // purely a closing brace (it reads at the outer level, not the inner one).
                depth = Math.min(depthBefore, braceDepth);
            }

            int keywordHits = countMatches(CONTROL_FLOW.matcher(trimmed));
            int logicalOps = countOccurrences(trimmed, "&&") + countOccurrences(trimmed, "||");

            double score = depth + keywordHits * 1.5 + logicalOps * 0.5;

            String severity;
            String reason;
            if (score >= 5) {
                severity = "HIGH";
                reason = "Deep nesting (level " + depth + ") combined with branching logic";
            } else if (score >= 2.5) {
                severity = "MEDIUM";
                reason = keywordHits > 0
                        ? "Branching logic at nesting level " + depth
                        : "Nested block at level " + depth;
            } else {
                severity = "LOW";
                reason = "Simple statement";
            }

            result.add(new LineComplexity(i + 1, severity, reason));
        }

        return result;
    }

    private int indentLevel(String rawLine) {
        int spaces = 0;
        for (char c : rawLine.toCharArray()) {
            if (c == ' ') {
                spaces++;
            } else if (c == '\t') {
                spaces += 4;
            } else {
                break;
            }
        }
        return spaces / 4;
    }

    private boolean isCommentOnly(String trimmed, SupportedLanguage language) {
        if (language == SupportedLanguage.PYTHON) {
            return trimmed.startsWith("#");
        }
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*");
    }

    private int countMatches(Matcher matcher) {
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return count;
    }
}
