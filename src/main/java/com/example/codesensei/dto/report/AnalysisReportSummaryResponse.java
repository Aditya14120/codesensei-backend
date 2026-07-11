package com.example.codesensei.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Lightweight row for the history list — no full analysis payload, so paginated lists stay cheap. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportSummaryResponse {
    private String id;
    private String fileName;
    private String language;
    private Double score;
    private String summaryExcerpt;
    private Instant createdAt;
}
