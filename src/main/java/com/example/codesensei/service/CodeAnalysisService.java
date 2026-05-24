package com.example.codesensei.service;

import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.service.ai.GoogleAIClient;
import com.example.codesensei.service.analyzer.CheckstyleAnalyzer;
import com.example.codesensei.service.analyzer.PMDAnalyzer;
import com.example.codesensei.service.analyzer.SpotBugsAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.codesensei.entity.User;
import com.example.codesensei.repository.UserRepository;
import com.example.codesensei.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.codesensei.model.AiReview;

import java.util.Collections;

@Service
public class CodeAnalysisService {

    @Autowired(required = false)
    private PMDAnalyzer pmdAnalyzer;

    @Autowired(required = false)
    private SpotBugsAnalyzer spotBugsAnalyzer;

    @Autowired(required = false)
    private CheckstyleAnalyzer checkstyleAnalyzer;

    @Autowired(required = false)
    private GoogleAIClient googleAIClient;

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private UserRepository userRepository;

    

    public CodeAnalysisResponse analyzeCode(String code) {
        CodeAnalysisResponse response = new CodeAnalysisResponse();

        // ========== PMD ==========
        try {
            if (pmdAnalyzer != null) {
                response.setPmdResults(pmdAnalyzer.analyze(code));
            } else {
                response.setPmdResults(Collections.singletonList("PMD Analyzer not configured."));
            }
        } catch (Exception e) {
            response.setPmdResults(Collections.singletonList("PMD analysis error: " + e.getMessage()));
        }

        // ========== CHECKSTYLE ==========
        try {
            if (checkstyleAnalyzer != null) {
                response.setCheckstyleResults(checkstyleAnalyzer.analyze(code));
            } else {
                response.setCheckstyleResults(Collections.singletonList("Checkstyle Analyzer not configured."));
            }
        } catch (Exception e) {
            response.setCheckstyleResults(Collections.singletonList("Checkstyle analysis error: " + e.getMessage()));
        }

        // ========== SPOTBUGS ==========
        try {
            if (spotBugsAnalyzer != null) {
                response.setSpotBugsResults(spotBugsAnalyzer.analyze(code));
            } else {
                response.setSpotBugsResults(Collections.singletonList("SpotBugs Analyzer not configured."));
            }
        } catch (Exception e) {
            response.setSpotBugsResults(Collections.singletonList("SpotBugs analysis error: " + e.getMessage()));
        }

        ObjectMapper mapper = new ObjectMapper();

        // ========== AI (Gemini) ==========
        try {
            if (googleAIClient != null) {

          String aiJson = googleAIClient.getCodeExplanation(code);

          AiReview aiReview = mapper.readValue(aiJson, AiReview.class);
          response.setAiExplanation(aiReview.getSummary());
          response.setSuggestions(aiReview.getImprovements());
          double finalScore = calculateFinalScore(response, aiReview.getScore());
          response.setScore(finalScore);
          response.setImprovedCode(aiReview.getImprovedCode());
}   
       else {
                response.setAiExplanation("AI Client not configured.");
                response.setSuggestions(Collections.emptyList());
            }
        } catch (Exception e) {
            response.setAiExplanation("AI analysis error: " + e.getMessage());
            response.setSuggestions(Collections.emptyList());
        }
        // ========== Save Report ==========
        try {
            User user=userRepository.findAll().stream().findFirst().orElse(null); // For demo, we take the first user. In real app, get from auth context.
            if(user!=null){
                String issues=buildIssuesSummary(response);
                String aiFeedback=response.getAiExplanation();
                reportService.saveReport(
                    "Codesnippet.java",
                    issues,
                    aiFeedback,
                    user
                );
            }
        } catch (Exception e) {
            System.out.println("Report save failed: " + e.getMessage());
        }

        return response;
    }
    private String buildIssuesSummary(CodeAnalysisResponse response){
        StringBuilder sb=new StringBuilder();
        sb.append("PMD Issues:\n");
        response.getPmdResults().forEach(issue->sb.append("- ").append(issue).append("\n"));
        sb.append("Checkstyle Issues:\n");
        response.getCheckstyleResults().forEach(issue->sb.append("- ").append(issue).append("\n"));
        sb.append("SpotBugs Issues:\n");
        response.getSpotBugsResults().forEach(issue->sb.append("- ").append(issue).append("\n"));
        return sb.toString();
    }
    private double calculateFinalScore(CodeAnalysisResponse response, double aiScore) {

    double finalScore = aiScore * 0.4;

    int pmdIssues = response.getPmdResults() != null
            ? response.getPmdResults().size()
            : 0;

    int checkstyleIssues = response.getCheckstyleResults() != null
            ? response.getCheckstyleResults().size()
            : 0;

    int spotbugsIssues = response.getSpotBugsResults() != null
            ? response.getSpotBugsResults().size()
            : 0;

    double pmdScore = Math.max(0, 10 - pmdIssues);
    double checkstyleScore = Math.max(0, 10 - checkstyleIssues);
    double spotbugsScore = Math.max(0, 10 - spotbugsIssues);

    finalScore += pmdScore * 0.2;
    finalScore += checkstyleScore * 0.2;
    finalScore += spotbugsScore * 0.2;

    return Math.round(finalScore * 10.0) / 10.0;
}
}
