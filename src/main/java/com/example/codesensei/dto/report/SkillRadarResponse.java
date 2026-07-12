package com.example.codesensei.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Powers the Skill Radar chart: the most recent report's scores vs. the average across history. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRadarResponse {
    private CategoryScores latest;
    private CategoryScores average;
    private int reportCount;
}
