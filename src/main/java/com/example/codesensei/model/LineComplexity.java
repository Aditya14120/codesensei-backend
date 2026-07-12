package com.example.codesensei.model;

/** One line's complexity rating for the frontend heatmap ("LOW"/"MEDIUM"/"HIGH"). */
public class LineComplexity {

    private int line;
    private String severity;
    private String reason;

    public LineComplexity() {}

    public LineComplexity(int line, String severity, String reason) {
        this.line = line;
        this.severity = severity;
        this.reason = reason;
    }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
