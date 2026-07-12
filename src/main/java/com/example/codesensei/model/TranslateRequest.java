package com.example.codesensei.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO for incoming cross-language translation requests. */
public class TranslateRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50000, message = "Code must be 50000 characters or fewer")
    private String code;

    @NotBlank(message = "Source language is required")
    private String sourceLanguage;

    @NotBlank(message = "Target language is required")
    private String targetLanguage;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
}
