package com.example.codesensei.dto.codefile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Deliberately excludes storagePath — clients never need or should see the server's disk layout. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeFileResponse {
    private String id;
    private String filename;
    private long size;
    private Instant uploadedAt;
}
