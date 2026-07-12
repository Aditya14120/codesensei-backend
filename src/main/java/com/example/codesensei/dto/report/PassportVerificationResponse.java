package com.example.codesensei.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Public, minimal-disclosure response for verifying a shared Code Quality Passport —
 * deliberately excludes the code and full analysis, since this endpoint has no auth check.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassportVerificationResponse {
    private String reportId;
    private String language;
    private Double score;
    private Instant createdAt;
}
