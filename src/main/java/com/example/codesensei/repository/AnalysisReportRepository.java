package com.example.codesensei.repository;

import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, String> {

    List<AnalysisReport> findByUser(User user);
}
