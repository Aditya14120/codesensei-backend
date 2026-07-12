package com.example.codesensei.service.impl;

import com.example.codesensei.dto.report.CategoryScores;
import com.example.codesensei.dto.report.PassportVerificationResponse;
import com.example.codesensei.dto.report.SkillRadarResponse;
import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.entity.User;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.model.SupportedLanguage;
import com.example.codesensei.repository.AnalysisReportRepository;
import com.example.codesensei.service.ReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final AnalysisReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    public ReportServiceImpl(AnalysisReportRepository reportRepository, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisReport saveReport(SupportedLanguage language, CodeAnalysisResponse response, User user) {

        String fullResponseJson;
        try {
            fullResponseJson = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize analysis response", e);
        }

        AnalysisReport report = AnalysisReport.builder()
                .fileName(language.getDefaultFileName())
                .issues(response.getSummary())
                .aiFeedback(response.getImprovements() != null ? response.getImprovements().toString() : "[]")
                .language(language.getId())
                .score(response.getScore())
                .fullResponseJson(fullResponseJson)
                .createdAt(Instant.now())
                .user(user)
                .build();

        AnalysisReport savedReport = reportRepository.saveAndFlush(report);

        if (savedReport.getId() == null || !reportRepository.existsById(savedReport.getId())) {
            throw new IllegalStateException("Report persistence verification failed");
        }

        log.info("Verified persisted analysis report {}", savedReport.getId());

        return savedReport;
    }

    @Override
    public Page<AnalysisReport> getUserReports(User user, Pageable pageable) {
        return reportRepository.findByUser(user, pageable);
    }

    @Override
    public Page<AnalysisReport> getUserReportsByLanguage(User user, String language, Pageable pageable) {
        return reportRepository.findByUserAndLanguage(user, language, pageable);
    }

    @Override
    public AnalysisReport getUserReportById(User user, String reportId) {
        return reportRepository.findByIdAndUser(reportId, user)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }

    @Override
    public SkillRadarResponse getSkillRadar(User user) {

        List<AnalysisReport> reports = reportRepository.findByUserOrderByCreatedAtDesc(user);

        List<CategoryScores> allScores = new ArrayList<>();
        for (AnalysisReport report : reports) {
            CategoryScores scores = parseCategoryScores(report);
            if (scores != null) {
                allScores.add(scores);
            }
        }

        if (allScores.isEmpty()) {
            CategoryScores empty = new CategoryScores(0, 0, 0, 0, 0);
            return new SkillRadarResponse(empty, empty, 0);
        }

        CategoryScores latest = allScores.get(0);
        CategoryScores average = averageScores(allScores);

        return new SkillRadarResponse(latest, average, allScores.size());
    }

    @Override
    public CategoryScores getCategoryScoresForReport(User user, String reportId) {
        AnalysisReport report = getUserReportById(user, reportId);
        CategoryScores scores = parseCategoryScores(report);
        if (scores == null) {
            throw new IllegalStateException("Could not compute category scores for report " + reportId);
        }
        return scores;
    }

    @Override
    public PassportVerificationResponse verifyReport(String reportId) {
        AnalysisReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        return new PassportVerificationResponse(
                report.getId(), report.getLanguage(), report.getScore(), report.getCreatedAt());
    }

    private CategoryScores parseCategoryScores(AnalysisReport report) {
        try {
            CodeAnalysisResponse analysis =
                    objectMapper.readValue(report.getFullResponseJson(), CodeAnalysisResponse.class);
            return computeCategoryScores(analysis);
        } catch (Exception e) {
            log.warn("Could not parse stored analysis for report {} while building skill radar: {}",
                    report.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Maps each category to the report data that actually represents it: security/bug/performance
     * issues map 1:1 to their own AI-review lists, readability maps to code smells, and best
     * practices combines suggested improvements with the static-analyzer (PMD/Checkstyle) hits —
     * the two tools most associated with style/best-practice linting.
     */
    private CategoryScores computeCategoryScores(CodeAnalysisResponse analysis) {

        int bestPracticeSignals = sizeOf(analysis.getImprovements())
                + countActionable(analysis.getPmdResults())
                + countActionable(analysis.getCheckstyleResults());

        return new CategoryScores(
                categoryScore(sizeOf(analysis.getSecurityIssues())),
                categoryScore(sizeOf(analysis.getCodeSmells())),
                categoryScore(sizeOf(analysis.getPerformanceIssues())),
                categoryScore(sizeOf(analysis.getBugs())),
                categoryScore(bestPracticeSignals));
    }

    private double categoryScore(int issueCount) {
        double score = 10 - issueCount * 2.0;
        return round1(Math.max(0, Math.min(10, score)));
    }

    private int sizeOf(List<String> values) {
        return values == null ? 0 : values.size();
    }

    // Static analyzers report placeholder strings ("PMD not configured") when disabled — those
    // aren't real findings and shouldn't count against the best-practices score.
    private int countActionable(List<String> issues) {
        if (issues == null) {
            return 0;
        }

        int count = 0;
        for (String issue : issues) {
            if (issue == null || issue.trim().isEmpty()) {
                continue;
            }
            String normalized = issue.toLowerCase(Locale.ROOT);
            if (!normalized.contains("no ") && !normalized.contains("not configured")
                    && !normalized.contains("dummy issue")) {
                count++;
            }
        }
        return count;
    }

    private CategoryScores averageScores(List<CategoryScores> scores) {
        double security = 0;
        double readability = 0;
        double performance = 0;
        double bugPrevention = 0;
        double bestPractices = 0;

        for (CategoryScores s : scores) {
            security += s.getSecurity();
            readability += s.getReadability();
            performance += s.getPerformance();
            bugPrevention += s.getBugPrevention();
            bestPractices += s.getBestPractices();
        }

        int n = scores.size();
        return new CategoryScores(
                round1(security / n), round1(readability / n), round1(performance / n),
                round1(bugPrevention / n), round1(bestPractices / n));
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
