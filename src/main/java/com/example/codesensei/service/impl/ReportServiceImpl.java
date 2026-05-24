package com.example.codesensei.service.impl;

import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.entity.User;
import com.example.codesensei.repository.AnalysisReportRepository;
import com.example.codesensei.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final AnalysisReportRepository reportRepository;

    public ReportServiceImpl(AnalysisReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public AnalysisReport saveReport(String fileName, String issues, String aiFeedback, User user) {

        AnalysisReport report = AnalysisReport.builder()
                .fileName(fileName)
                .issues(issues)
                .aiFeedback(aiFeedback)
                .createdAt(Instant.now())
                .user(user)
                .build();

        return reportRepository.save(report);
    }

    @Override
    public List<AnalysisReport> getUserReports(User user) {
        return reportRepository.findByUser(user);
    }
}
