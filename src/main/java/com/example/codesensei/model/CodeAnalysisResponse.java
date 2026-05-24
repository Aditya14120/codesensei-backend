package com.example.codesensei.model;

import java.util.List;

/**
 * DTO returned by /api/code/analyze.
 * Holds AI explanation, suggestions and results from static analyzers.
 */
public class CodeAnalysisResponse {
    private String aiExplanation;               // textual AI explanation of code
    private List<String> suggestions;           // AI improvement suggestions
    private List<String> pmdResults;            // PMD findings
    private List<String> checkstyleResults;     // Checkstyle findings
    private List<String> spotBugsResults;   
    private double score;
    private String improvedCode;    // SpotBugs findings

    public CodeAnalysisResponse() {}

    public CodeAnalysisResponse(String aiExplanation, List<String> suggestions,
                                List<String> pmdResults, List<String> checkstyleResults,
                                List<String> spotBugsResults) {
        this.aiExplanation = aiExplanation;
        this.suggestions = suggestions;
        this.pmdResults = pmdResults;
        this.checkstyleResults = checkstyleResults;
        this.spotBugsResults = spotBugsResults;
    }

    // Getters / setters
    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public List<String> getPmdResults() { return pmdResults; }
    public void setPmdResults(List<String> pmdResults) { this.pmdResults = pmdResults; }

    public List<String> getCheckstyleResults() { return checkstyleResults; }
    public void setCheckstyleResults(List<String> checkstyleResults) { this.checkstyleResults = checkstyleResults; }

    public List<String> getSpotBugsResults() { return spotBugsResults; }
    public void setSpotBugsResults(List<String> spotBugsResults) { this.spotBugsResults = spotBugsResults; }

    public double getScore() {
    return score;
}

public void setScore(double score) {
    this.score = score;
}

public String getImprovedCode() {
    return improvedCode;
}

public void setImprovedCode(String improvedCode) {
    this.improvedCode = improvedCode;
}
}
