package com.example.codesensei.model;

/**
 * DTO for incoming analysis requests.
 * Minimal (single `code` field) — extend later with language, filename, options, userId, etc.
 */
public class CodeAnalysisRequest {
    private String code;

    public CodeAnalysisRequest() {}

    public CodeAnalysisRequest(String code) { this.code = code; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public String toString() {
        if (code == null) return "CodeAnalysisRequest{code=null}";
        String preview = code.length() > 120 ? code.substring(0,120) + "..." : code;
        return "CodeAnalysisRequest{codePreview=\"" + preview.replace("\n","\\n") + "\"}";
    }
}
