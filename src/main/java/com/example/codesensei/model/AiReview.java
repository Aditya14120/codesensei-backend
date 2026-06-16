package com.example.codesensei.model;

import java.util.Collections;
import java.util.List;

public class AiReview {

    private double score;

    private String summary;

    private List<String> bugs;

    private List<String> codeSmells;

    private List<String> performanceIssues;

    private List<String> securityIssues;

    private List<String> improvements;

    private String improvedCode;

    private List<String> learningTips;

    public AiReview() {
        this.bugs = Collections.emptyList();
        this.codeSmells = Collections.emptyList();
        this.performanceIssues = Collections.emptyList();
        this.securityIssues = Collections.emptyList();
        this.improvements = Collections.emptyList();
        this.learningTips = Collections.emptyList();
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
}
