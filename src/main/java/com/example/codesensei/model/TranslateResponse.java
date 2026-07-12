package com.example.codesensei.model;

/** Response for a cross-language code translation. */
public class TranslateResponse {

    private String translatedCode;
    private String sourceLanguage;
    private String targetLanguage;

    public TranslateResponse() {}

    public TranslateResponse(String translatedCode, String sourceLanguage, String targetLanguage) {
        this.translatedCode = translatedCode;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public String getTranslatedCode() { return translatedCode; }
    public void setTranslatedCode(String translatedCode) { this.translatedCode = translatedCode; }

    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
}
