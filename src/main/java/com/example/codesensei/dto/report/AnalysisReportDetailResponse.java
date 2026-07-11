package com.example.codesensei.dto.report;

import com.example.codesensei.model.CodeAnalysisResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Full report detail — {@code analysis} is shaped identically to a live /api/code/analyze
 *  response, so the frontend can reopen it in the same AnalysisDashboard with no changes. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportDetailResponse {
    private String id;
    private Instant createdAt;
    private CodeAnalysisResponse analysis;
}
