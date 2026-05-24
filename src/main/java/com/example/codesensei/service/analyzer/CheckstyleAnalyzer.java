package com.example.codesensei.service.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs Checkstyle analysis on provided Java code.
 * Currently returns dummy data for demonstration.
 */
@Component
public class CheckstyleAnalyzer {
    public List<String> analyze(String code) {
        List<String> issues = new ArrayList<>();
        issues.add("[Checkstyle] Dummy issue: Line is longer than 120 characters");
        return issues;
    }
}
