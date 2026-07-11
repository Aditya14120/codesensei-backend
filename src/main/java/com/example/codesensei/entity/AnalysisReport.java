package com.example.codesensei.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "analysis_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String issues;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    /** Language id (java/python/c/cpp) the analysis was run against; used for filtering history. */
    private String language;

    private Double score;

    /**
     * The full {@code CodeAnalysisResponse} as JSON, so a past report can be reopened with the
     * same fidelity as a fresh analysis (bugs, improved code, learning tips, etc.) instead of
     * just the summary/improvements excerpt that {@link #issues}/{@link #aiFeedback} carry.
     */
    @Column(columnDefinition = "TEXT")
    private String fullResponseJson;

    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
