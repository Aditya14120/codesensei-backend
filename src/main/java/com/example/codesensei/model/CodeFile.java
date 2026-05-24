package com.example.codesensei.model;

import java.time.Instant;

/**
 * Simple DTO / model representing a stored code file.
 * This is intentionally minimal - add DB annotations later if you persist this in a database.
 */
public class CodeFile {
    private String id;
    private String filename;
    private String storagePath; // local path or S3 key
    private long size;
    private Instant uploadedAt;

    public CodeFile() {}

    public CodeFile(String id, String filename, String storagePath, long size, Instant uploadedAt) {
        this.id = id;
        this.filename = filename;
        this.storagePath = storagePath;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
