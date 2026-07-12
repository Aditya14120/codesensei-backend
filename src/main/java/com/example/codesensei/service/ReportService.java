package com.example.codesensei.service;

import com.example.codesensei.dto.report.CategoryScores;
import com.example.codesensei.dto.report.PassportVerificationResponse;
import com.example.codesensei.dto.report.SkillRadarResponse;
import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.entity.User;
import com.example.codesensei.model.CodeAnalysisResponse;
import com.example.codesensei.model.SupportedLanguage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    AnalysisReport saveReport(SupportedLanguage language, CodeAnalysisResponse response, User user);

    Page<AnalysisReport> getUserReports(User user, Pageable pageable);

    Page<AnalysisReport> getUserReportsByLanguage(User user, String language, Pageable pageable);

    AnalysisReport getUserReportById(User user, String reportId);

    SkillRadarResponse getSkillRadar(User user);

    CategoryScores getCategoryScoresForReport(User user, String reportId);

    /** No owner check — this backs the public passport-verification link. */
    PassportVerificationResponse verifyReport(String reportId);
}
