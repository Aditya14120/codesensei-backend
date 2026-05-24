package com.example.codesensei.service.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs SpotBugs analysis on provided Java code.
 * Currently returns dummy data for demonstration.
 */
@Component
public class SpotBugsAnalyzer {
    public List<String> analyze(String code) {
        List<String> issues = new ArrayList<>();
        issues.add("[SpotBugs] Dummy issue: Possible null pointer dereference");
        return issues;
    }
}
