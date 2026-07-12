package com.example.codesensei.model;

/** Shape of the JSON the AI returns for a translation request — deliberately minimal. */
public class TranslationResult {

    private String translatedCode;

    public String getTranslatedCode() { return translatedCode; }
    public void setTranslatedCode(String translatedCode) { this.translatedCode = translatedCode; }
}
