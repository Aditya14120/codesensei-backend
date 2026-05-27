package com.example.codesensei.model;

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
        this.bugs = bugs;
    }

    public List<String> getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(List<String> codeSmells) {
        this.codeSmells = codeSmells;
    }

    public List<String> getPerformanceIssues() {
        return performanceIssues;
    }

    public void setPerformanceIssues(List<String> performanceIssues) {
        this.performanceIssues = performanceIssues;
    }

    public List<String> getSecurityIssues() {
        return securityIssues;
    }

    public void setSecurityIssues(List<String> securityIssues) {
        this.securityIssues = securityIssues;
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements;
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
        this.learningTips = learningTips;
    }
}