package com.example.codesensei.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for incoming analysis requests.
 * Minimal (single `code` field) — extend later with language, filename, options, userId, etc.
 */
public class CodeAnalysisRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50000, message = "Code must be 50000 characters or fewer")
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
