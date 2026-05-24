package com.example.codesensei.service;

import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.entity.User;

import java.util.List;

public interface ReportService {

    AnalysisReport saveReport(String fileName, String issues, String aiFeedback, User user);

    List<AnalysisReport> getUserReports(User user);
}
