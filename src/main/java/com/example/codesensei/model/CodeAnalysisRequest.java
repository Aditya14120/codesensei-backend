package com.example.codesensei.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for incoming analysis requests.
 */
public class CodeAnalysisRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50000, message = "Code must be 50000 characters or fewer")
    private String code;

    /** Source language: java, python, c, or cpp. Defaults to java if omitted. */
    private String language;

    public CodeAnalysisRequest() {}

    public CodeAnalysisRequest(String code) { this.code = code; }

    public CodeAnalysisRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    @Override
    public String toString() {
        if (code == null) return "CodeAnalysisRequest{code=null}";
        String preview = code.length() > 120 ? code.substring(0,120) + "..." : code;
        return "CodeAnalysisRequest{language=" + language + ", codePreview=\"" + preview.replace("\n","\\n") + "\"}";
    }
}
