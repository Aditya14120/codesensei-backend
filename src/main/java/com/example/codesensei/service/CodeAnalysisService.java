package com.example.codesensei.service;

import com.example.codesensei.entity.User;
import com.example.codesensei.model.AiReview;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.model.SupportedLanguage;
import com.example.codesensei.service.ai.GroqAIClient;
import com.example.codesensei.service.analyzer.CheckstyleAnalyzer;
import com.example.codesensei.service.analyzer.PMDAnalyzer;
import com.example.codesensei.service.analyzer.SpotBugsAnalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class CodeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CodeAnalysisService.class);

    @Autowired(required = false)
    private PMDAnalyzer pmdAnalyzer;

    @Autowired(required = false)
    private SpotBugsAnalyzer spotBugsAnalyzer;

    @Autowired(required = false)
    private CheckstyleAnalyzer checkstyleAnalyzer;

    @Autowired
    private GroqAIClient groqAIClient;

    @Autowired
    private ReportService reportService;

    public CodeAnalysisResponse analyzeCode(String code, String languageId, User currentUser) {

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        SupportedLanguage language = SupportedLanguage.fromId(languageId);

        CodeAnalysisResponse response = new CodeAnalysisResponse();
        response.setLanguage(language.getId());
        response.setOriginalCode(code);

        try {

            response.setPmdResults(
                    pmdAnalyzer != null
                            ? pmdAnalyzer.analyze(code, language)
                            : Collections.singletonList("PMD not configured"));

            response.setCheckstyleResults(
                    checkstyleAnalyzer != null
                            ? checkstyleAnalyzer.analyze(code, language)
                            : Collections.singletonList("Checkstyle not configured"));

            response.setSpotBugsResults(
                    spotBugsAnalyzer != null
                            ? spotBugsAnalyzer.analyze(code, language)
                            : Collections.singletonList("SpotBugs not configured"));

            AiReview aiReview = normalizeAiReview(groqAIClient.getCodeReview(code, language), code);

            response.setScore(
                    calculateFinalScore(response, aiReview.getScore()));

            response.setSummary(aiReview.getSummary());

            response.setBugs(aiReview.getBugs());

            response.setCodeSmells(aiReview.getCodeSmells());

            response.setPerformanceIssues(aiReview.getPerformanceIssues());

            response.setSecurityIssues(aiReview.getSecurityIssues());

            response.setImprovements(aiReview.getImprovements());

            response.setImprovedCode(aiReview.getImprovedCode());

            response.setLearningTips(aiReview.getLearningTips());

            saveReport(response, language, currentUser);

        } catch (Exception e) {

            log.error("Code analysis failed", e);

            response.setSummary("Analysis failed: " + e.getMessage());

            response.setScore(0);

            response.setImprovements(Collections.emptyList());

            response.setImprovedCode(code);

            response.setPmdResults(emptyIfNull(response.getPmdResults()));

            response.setCheckstyleResults(emptyIfNull(response.getCheckstyleResults()));

            response.setSpotBugsResults(emptyIfNull(response.getSpotBugsResults()));
        }

        return response;
    }

    private double calculateFinalScore(CodeAnalysisResponse response, double aiScore) {

        int pmd = countActionableIssues(response.getPmdResults());

        int checkstyle = countActionableIssues(response.getCheckstyleResults());

        int spotbugs = countActionableIssues(response.getSpotBugsResults());

        double analyzerPenalty = (pmd + checkstyle + spotbugs) * 0.35;

        double finalScore = aiScore - analyzerPenalty;

        finalScore = Math.max(0, Math.min(10, finalScore));

        return Math.round(finalScore * 10.0) / 10.0;
    }

    private int countActionableIssues(List<String> issues) {

        if (issues == null) {
            return 0;
        }

        int count = 0;

        for (String issue : issues) {
            if (isActionableIssue(issue)) {
                count++;
            }
        }

        return count;
    }

    private boolean isActionableIssue(String issue) {

        if (issue == null || issue.trim().isEmpty()) {
            return false;
        }

        String normalized = issue.toLowerCase(Locale.ROOT);

        return !normalized.contains("no ")
                && !normalized.contains("not configured")
                && !normalized.contains("dummy issue");
    }

    private AiReview normalizeAiReview(AiReview review, String originalCode) {

        AiReview normalized = review != null ? review : new AiReview();

        normalized.setScore(Math.max(0, Math.min(10, normalized.getScore())));
        normalized.setSummary(defaultString(normalized.getSummary(), "AI analysis completed with limited feedback."));
        normalized.setBugs(emptyIfNull(normalized.getBugs()));
        normalized.setCodeSmells(emptyIfNull(normalized.getCodeSmells()));
        normalized.setPerformanceIssues(emptyIfNull(normalized.getPerformanceIssues()));
        normalized.setSecurityIssues(emptyIfNull(normalized.getSecurityIssues()));
        normalized.setImprovements(emptyIfNull(normalized.getImprovements()));
        normalized.setImprovedCode(defaultString(normalized.getImprovedCode(), originalCode));
        normalized.setLearningTips(emptyIfNull(normalized.getLearningTips()));

        if (normalized.getScore() == 0 && hasUsefulFeedback(normalized)) {
            normalized.setScore(generateHeuristicScore(normalized));
        }

        return normalized;
    }

    private boolean hasUsefulFeedback(AiReview review) {
        return !review.getImprovements().isEmpty()
                || !review.getBugs().isEmpty()
                || !review.getCodeSmells().isEmpty()
                || !review.getPerformanceIssues().isEmpty()
                || !review.getSecurityIssues().isEmpty();
    }

    private double generateHeuristicScore(AiReview review) {

        int issueCount = review.getBugs().size()
                + review.getCodeSmells().size()
                + review.getPerformanceIssues().size()
                + review.getSecurityIssues().size();

        double score = 8.5 - (issueCount * 0.6);

        return Math.round(Math.max(1, Math.min(10, score)) * 10.0) / 10.0;
    }

    private List<String> emptyIfNull(List<String> values) {
        return values == null
                ? Collections.emptyList()
                : new ArrayList<>(values.stream().filter(Objects::nonNull).toList());
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void saveReport(CodeAnalysisResponse response, SupportedLanguage language, User currentUser) {

        try {

            if (currentUser != null) {

                var savedReport = reportService.saveReport(language, response, currentUser);

                if (savedReport != null && savedReport.getId() != null) {
                    log.info("Analysis report persisted with id {} for user {}", savedReport.getId(), currentUser.getId());
                } else {
                    log.warn("Analysis report persistence returned without an id");
                }
            } else {
                log.info("Skipping report persistence because no authenticated user is present");
            }

        } catch (Exception e) {

            log.warn("Report save failed: {}", e.getMessage());
        }
    }
}
