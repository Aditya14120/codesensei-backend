package com.example.codesensei.model;

/**
 * Languages the analyzers and AI client know how to review.
 * Unrecognized/blank input normalizes to JAVA to preserve existing behavior.
 */
public enum SupportedLanguage {

    JAVA("java", "Java", "java", "Main.java"),
    PYTHON("python", "Python", "py", "main.py"),
    C("c", "C", "c", "main.c"),
    CPP("cpp", "C++", "cpp", "main.cpp");

    private final String id;
    private final String displayName;
    private final String extension;
    private final String defaultFileName;

    SupportedLanguage(String id, String displayName, String extension, String defaultFileName) {
        this.id = id;
        this.displayName = displayName;
        this.extension = extension;
        this.defaultFileName = defaultFileName;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getExtension() { return extension; }
    public String getDefaultFileName() { return defaultFileName; }

    public static SupportedLanguage fromId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return JAVA;
        }

        String normalized = rawId.trim().toLowerCase();

        for (SupportedLanguage language : values()) {
            if (language.id.equals(normalized)) {
                return language;
            }
        }

        if ("c++".equals(normalized) || "cplusplus".equals(normalized)) {
            return CPP;
        }
        if ("py".equals(normalized)) {
            return PYTHON;
        }

        return JAVA;
    }
}
