package com.example.codesensei.service.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs Checkstyle analysis on provided Java code.
 * Lightweight style checks until a full Checkstyle ruleset is wired in.
 */
@Component
public class CheckstyleAnalyzer {
    public List<String> analyze(String code) {
        List<String> issues = new ArrayList<>();

        if (code == null || code.trim().isEmpty()) {
            issues.add("[Checkstyle] Code is empty.");
            return issues;
        }

        String[] lines = code.split("\\R", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.length() > 120) {
                issues.add("[Checkstyle] Line " + (i + 1) + " is longer than 120 characters.");
            }

            if (line.endsWith(" ") || line.endsWith("\t")) {
                issues.add("[Checkstyle] Line " + (i + 1) + " has trailing whitespace.");
            }
        }

        if (!code.contains("public class") && !code.contains("class ")) {
            issues.add("[Checkstyle] Java code should declare a class.");
        }

        if (issues.isEmpty()) {
            issues.add("No Checkstyle issues found.");
        }

        return issues;
    }
}
