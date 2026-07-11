package com.example.codesensei.service.impl;

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
}
