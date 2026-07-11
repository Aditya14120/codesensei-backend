package com.example.codesensei.model;

import java.util.Collections;
import java.util.List;

public class CodeAnalysisResponse {

    private String aiExplanation;

    private String language;

    /** The code as submitted, so the frontend can render a before/after diff against improvedCode. */
    private String originalCode;

    private List<String> suggestions;

    private double score;

    private String summary;

    private List<String> bugs;

    private List<String> codeSmells;

    private List<String> performanceIssues;

    private List<String> securityIssues;

    private List<String> improvements;

    private String improvedCode;

    private List<String> learningTips;

    private List<String> pmdResults;

    private List<String> checkstyleResults;

    private List<String> spotBugsResults;

    public CodeAnalysisResponse() {
        this.bugs = Collections.emptyList();
        this.codeSmells = Collections.emptyList();
        this.performanceIssues = Collections.emptyList();
        this.securityIssues = Collections.emptyList();
        this.improvements = Collections.emptyList();
        this.suggestions = Collections.emptyList();
        this.learningTips = Collections.emptyList();
        this.pmdResults = Collections.emptyList();
        this.checkstyleResults = Collections.emptyList();
        this.spotBugsResults = Collections.emptyList();
    }

    public CodeAnalysisResponse(String aiExplanation, List<String> suggestions,
            List<String> pmdResults, List<String> checkstyleResults, List<String> spotBugsResults) {
        this();
        this.aiExplanation = aiExplanation;
        this.summary = aiExplanation;
        setSuggestions(suggestions);
        setImprovements(suggestions);
        setPmdResults(pmdResults);
        setCheckstyleResults(checkstyleResults);
        setSpotBugsResults(spotBugsResults);
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : Collections.emptyList();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
        this.aiExplanation = summary;
    }

    public List<String> getBugs() {
        return bugs;
    }

    public void setBugs(List<String> bugs) {
        this.bugs = bugs != null ? bugs : Collections.emptyList();
    }

    public List<String> getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(List<String> codeSmells) {
        this.codeSmells = codeSmells != null ? codeSmells : Collections.emptyList();
    }

    public List<String> getPerformanceIssues() {
        return performanceIssues;
    }

    public void setPerformanceIssues(List<String> performanceIssues) {
        this.performanceIssues = performanceIssues != null ? performanceIssues : Collections.emptyList();
    }

    public List<String> getSecurityIssues() {
        return securityIssues;
    }

    public void setSecurityIssues(List<String> securityIssues) {
        this.securityIssues = securityIssues != null ? securityIssues : Collections.emptyList();
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements != null ? improvements : Collections.emptyList();
        this.suggestions = this.improvements;
    }

    public String getImprovedCode() {
        return improvedCode;
    }

    public void setImprovedCode(String improvedCode) {
        this.improvedCode = improvedCode;
    }

    public List<String> getLearningTips() {
        return learningTips;
    }

    public void setLearningTips(List<String> learningTips) {
        this.learningTips = learningTips != null ? learningTips : Collections.emptyList();
    }

    public List<String> getPmdResults() {
        return pmdResults;
    }

    public void setPmdResults(List<String> pmdResults) {
        this.pmdResults = pmdResults != null ? pmdResults : Collections.emptyList();
    }

    public List<String> getCheckstyleResults() {
        return checkstyleResults;
    }

    public void setCheckstyleResults(List<String> checkstyleResults) {
        this.checkstyleResults = checkstyleResults != null ? checkstyleResults : Collections.emptyList();
    }

    public List<String> getSpotBugsResults() {
        return spotBugsResults;
    }

    public void setSpotBugsResults(List<String> spotBugsResults) {
        this.spotBugsResults = spotBugsResults != null ? spotBugsResults : Collections.emptyList();
    }
}
