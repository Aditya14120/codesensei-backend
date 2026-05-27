package com.example.codesensei.service;

import com.example.codesensei.entity.User;
import com.example.codesensei.model.AiReview;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.repository.UserRepository;
import com.example.codesensei.service.ai.GoogleAIClient;
import com.example.codesensei.service.analyzer.CheckstyleAnalyzer;
import com.example.codesensei.service.analyzer.PMDAnalyzer;
import com.example.codesensei.service.analyzer.SpotBugsAnalyzer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CodeAnalysisService {

    @Autowired(required = false)
    private PMDAnalyzer pmdAnalyzer;

    @Autowired(required = false)
    private SpotBugsAnalyzer spotBugsAnalyzer;

    @Autowired(required = false)
    private CheckstyleAnalyzer checkstyleAnalyzer;

    @Autowired
    private GoogleAIClient googleAIClient;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    public CodeAnalysisResponse analyzeCode(String code) {

        CodeAnalysisResponse response = new CodeAnalysisResponse();

        try {

            response.setPmdResults(
                    pmdAnalyzer != null
                            ? pmdAnalyzer.analyze(code)
                            : Collections.singletonList("PMD not configured"));

            response.setCheckstyleResults(
                    checkstyleAnalyzer != null
                            ? checkstyleAnalyzer.analyze(code)
                            : Collections.singletonList("Checkstyle not configured"));

            response.setSpotBugsResults(
                    spotBugsAnalyzer != null
                            ? spotBugsAnalyzer.analyze(code)
                            : Collections.singletonList("SpotBugs not configured"));

            AiReview aiReview = googleAIClient.getCodeReview(code);

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

            saveReport(response);

        } catch (Exception e) {

            response.setSummary("Analysis failed: " + e.getMessage());

            response.setScore(0);
        }

        return response;
    }

    private double calculateFinalScore(CodeAnalysisResponse response, double aiScore) {

        int pmd = response.getPmdResults() != null
                ? response.getPmdResults().size()
                : 0;

        int checkstyle = response.getCheckstyleResults() != null
                ? response.getCheckstyleResults().size()
                : 0;

        int spotbugs = response.getSpotBugsResults() != null
                ? response.getSpotBugsResults().size()
                : 0;

        double analyzerPenalty = (pmd + checkstyle + spotbugs) * 0.2;

        double finalScore = aiScore - analyzerPenalty;

        finalScore = Math.max(0, Math.min(10, finalScore));

        return Math.round(finalScore * 10.0) / 10.0;
    }

    private void saveReport(CodeAnalysisResponse response) {

        try {

            User user = userRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (user != null) {

                reportService.saveReport(
                        "CodeSnippet.java",
                        response.getSummary(),
                        response.getImprovements().toString(),
                        user);
            }

        } catch (Exception e) {

            System.out.println("Report save failed: " + e.getMessage());
        }
    }
}