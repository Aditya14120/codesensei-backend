package com.example.codesensei.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 0-10 scores per skill category, derived from a single report's issue counts. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScores {
    private double security;
    private double readability;
    private double performance;
    private double bugPrevention;
    private double bestPractices;
}
