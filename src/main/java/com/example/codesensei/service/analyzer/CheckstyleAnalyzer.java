package com.example.codesensei.service.analyzer;

import com.example.codesensei.model.SupportedLanguage;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs lightweight style checks per source language until a full
 * Checkstyle/pylint/cpplint ruleset is wired in.
 */
@Component
public class CheckstyleAnalyzer {

    public List<String> analyze(String code, SupportedLanguage language) {
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

        switch (language) {
            case PYTHON -> analyzePython(code, issues);
            case C -> analyzeC(code, issues);
            case CPP -> analyzeCpp(code, issues);
            default -> analyzeJava(code, issues);
        }

        if (issues.isEmpty()) {
            issues.add("No Checkstyle issues found.");
        }

        return issues;
    }

    private void analyzeJava(String code, List<String> issues) {
        if (!code.contains("public class") && !code.contains("class ")) {
            issues.add("[Checkstyle] Java code should declare a class.");
        }
    }

    private void analyzePython(String code, List<String> issues) {
        if (code.contains("\t") && code.contains("    ")) {
            issues.add("[Checkstyle] Mixing tabs and spaces for indentation.");
        }

        if (code.matches("(?s).*\\bdef [a-z]+[A-Z]\\w*\\(.*")) {
            issues.add("[Checkstyle] Python function names should use snake_case, not camelCase.");
        }
    }

    private void analyzeC(String code, List<String> issues) {
        if (code.contains("void main(")) {
            issues.add("[Checkstyle] 'main' should return int, not void.");
        }

        if (!code.contains("main(")) {
            issues.add("[Checkstyle] No 'main' entry point found.");
        }
    }

    private void analyzeCpp(String code, List<String> issues) {
        if (code.contains("void main(")) {
            issues.add("[Checkstyle] 'main' should return int, not void.");
        }

        if (!code.contains("main(")) {
            issues.add("[Checkstyle] No 'main' entry point found.");
        }
    }
}
