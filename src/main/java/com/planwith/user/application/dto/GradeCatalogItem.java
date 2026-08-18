package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradeCatalogItem {

    private final String gradeCode;
    private final String gradeName;
    private final int gradeLevel;
    private final String description;
    private final List<Condition> conditions;
    private final List<Benefit> benefits;

    @Getter
    @Builder
    public static class Condition {
        private final String metricType;
        private final String conditionName;
        private final long thresholdValue;
        private final int sortOrder;
        private final String description;
    }

    @Getter
    @Builder
    public static class Benefit {
        private final String benefitCode;
        private final String benefitName;
        private final String benefitValue;
        private final String description;
        private final int sortOrder;
    }
}
