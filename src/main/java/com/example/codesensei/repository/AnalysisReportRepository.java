package com.example.codesensei.repository;

import com.example.codesensei.entity.AnalysisReport;
import com.example.codesensei.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, String> {

    Page<AnalysisReport> findByUser(User user, Pageable pageable);

    Page<AnalysisReport> findByUserAndLanguage(User user, String language, Pageable pageable);

    // Scoping by owner in the query itself (not a post-fetch check) means a caller can never
    // even attempt to load another user's report by guessing its id.
    Optional<AnalysisReport> findByIdAndUser(String id, User user);
}
