package com.example.codesensei.service.analyzer;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PMDAnalyzer {

    public List<String> analyze(String code) {

        List<String> issues = new ArrayList<>();

        try {

            if (code == null || code.trim().isEmpty()) {
                issues.add("[PMD] Code is empty.");
                return issues;
            }

            // Basic custom analysis rules

            if (code.contains("System.out.println")) {
                issues.add("[PMD] Avoid using System.out.println in production code.");
            }

            if (code.contains("int x")) {
                issues.add("[PMD] Variable name 'x' is not meaningful.");
            }

            if (code.length() > 1000) {
                issues.add("[PMD] Method/class is too large.");
            }

            if (code.contains("catch(Exception")) {
                issues.add("[PMD] Avoid catching generic Exception.");
            }

            if (code.contains("== null")) {
                issues.add("[PMD] Consider better null handling.");
            }

        } catch (Exception e) {

            issues.add("PMD analysis failed: " + e.getMessage());

        }

        if (issues.isEmpty()) {
            issues.add("No PMD issues found.");
        }

        return issues;
    }
}
