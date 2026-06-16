package com.example.codesensei.service.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs SpotBugs analysis on provided Java code.
 * Lightweight bug-risk checks until full bytecode SpotBugs analysis is wired in.
 */
@Component
public class SpotBugsAnalyzer {
    public List<String> analyze(String code) {
        List<String> issues = new ArrayList<>();

        if (code == null || code.trim().isEmpty()) {
            issues.add("[SpotBugs] Code is empty.");
            return issues;
        }

        if (code.contains(".equals(") && code.contains("== null")) {
            issues.add("[SpotBugs] Review null checks near equals calls to avoid null pointer risks.");
        }

        if (code.contains("Thread.sleep(")) {
            issues.add("[SpotBugs] Avoid Thread.sleep in application logic unless interruption is handled carefully.");
        }

        if (code.contains("new Random()")) {
            issues.add("[SpotBugs] Repeated Random construction can produce weak randomness and unnecessary allocation.");
        }

        if (code.contains("catch (Exception") || code.contains("catch(Exception")) {
            issues.add("[SpotBugs] Catching generic Exception can hide defects and make recovery unclear.");
        }

        if (issues.isEmpty()) {
            issues.add("No SpotBugs issues found.");
        }

        return issues;
    }
}
