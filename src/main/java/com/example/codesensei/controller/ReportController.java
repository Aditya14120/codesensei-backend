package com.example.codesensei.controller;

import com.example.codesensei.dto.report.AnalysisReportDetailResponse;
import com.example.codesensei.dto.report.AnalysisReportSummaryResponse;
import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.security.CustomUserDetails;
import com.example.codesensei.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "score", "fileName");

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    public ReportController(ReportService reportService, ObjectMapper objectMapper) {
        this.reportService = reportService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<Page<AnalysisReportSummaryResponse>> getReports(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String language) {

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy must be one of " + SORTABLE_FIELDS);
        }

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<AnalysisReport> reports = (language == null || language.isBlank())
                ? reportService.getUserReports(principal.getUser(), pageable)
                : reportService.getUserReportsByLanguage(principal.getUser(), language, pageable);

        return ResponseEntity.ok(reports.map(this::toSummary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisReportDetailResponse> getReport(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String id) {

        AnalysisReport report = reportService.getUserReportById(principal.getUser(), id);
        return ResponseEntity.ok(toDetail(report));
    }

    private AnalysisReportSummaryResponse toSummary(AnalysisReport report) {
        String issues = report.getIssues();
        String excerpt = issues == null ? "" : (issues.length() > 160 ? issues.substring(0, 160) + "..." : issues);

        return new AnalysisReportSummaryResponse(
                report.getId(), report.getFileName(), report.getLanguage(),
                report.getScore(), excerpt, report.getCreatedAt());
    }

    private AnalysisReportDetailResponse toDetail(AnalysisReport report) {
        try {
            CodeAnalysisResponse analysis = objectMapper.readValue(report.getFullResponseJson(), CodeAnalysisResponse.class);
            return new AnalysisReportDetailResponse(report.getId(), report.getCreatedAt(), analysis);
        } catch (Exception e) {
            throw new IllegalStateException("Stored analysis for report " + report.getId() + " could not be read");
        }
    }
}
