package com.example.codesensei.service.analyzer;

import com.example.codesensei.model.SupportedLanguage;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Runs lightweight bug-risk checks per source language until full
 * bytecode/AST-based analysis (SpotBugs, bandit, cppcheck) is wired in.
 */
@Component
public class SpotBugsAnalyzer {

    public List<String> analyze(String code, SupportedLanguage language) {
        List<String> issues = new ArrayList<>();

        if (code == null || code.trim().isEmpty()) {
            issues.add("[SpotBugs] Code is empty.");
            return issues;
        }

        switch (language) {
            case PYTHON -> analyzePython(code, issues);
            case C -> analyzeC(code, issues);
            case CPP -> analyzeCpp(code, issues);
            default -> analyzeJava(code, issues);
        }

        if (issues.isEmpty()) {
            issues.add("No SpotBugs issues found.");
        }

        return issues;
    }

    private void analyzeJava(String code, List<String> issues) {

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
    }

    private void analyzePython(String code, List<String> issues) {

        if (code.contains("eval(")) {
            issues.add("[SpotBugs] Avoid eval(); it can execute arbitrary code and is a security risk.");
        }

        if (Pattern.compile("def\\s+\\w+\\([^)]*=\\s*(\\[]|\\{})").matcher(code).find()) {
            issues.add("[SpotBugs] Avoid mutable default arguments (e.g. []/{}) in function definitions.");
        }

        if (code.contains("except:")) {
            issues.add("[SpotBugs] Bare except clauses can hide defects and make recovery unclear.");
        }
    }

    private void analyzeC(String code, List<String> issues) {

        if (code.contains("strcpy(") || code.contains("strcat(") || code.contains("sprintf(")) {
            issues.add("[SpotBugs] Avoid unbounded string functions (strcpy/strcat/sprintf); prefer the 'n' variants.");
        }

        int mallocCount = countOccurrences(code, "malloc(") + countOccurrences(code, "calloc(");
        int freeCount = countOccurrences(code, "free(");

        if (mallocCount > freeCount) {
            issues.add("[SpotBugs] Possible memory leak: allocation calls exceed free() calls.");
        }
    }

    private void analyzeCpp(String code, List<String> issues) {

        if (code.contains("strcpy(") || code.contains("strcat(") || code.contains("sprintf(")) {
            issues.add("[SpotBugs] Avoid unbounded string functions (strcpy/strcat/sprintf); prefer safer alternatives.");
        }

        int newCount = countOccurrences(code, "new ");
        int deleteCount = countOccurrences(code, "delete ") + countOccurrences(code, "delete[]");

        if (newCount > deleteCount) {
            issues.add("[SpotBugs] Possible memory leak: 'new' calls exceed 'delete' calls; consider smart pointers.");
        }
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(token, index)) != -1) {
            count++;
            index += token.length();
        }

        return count;
    }
}
