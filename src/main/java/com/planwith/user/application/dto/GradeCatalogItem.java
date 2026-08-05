package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradeCatalogItem {

    private final String gradeCode;
    private final String nameKo;
    private final int sortOrder;
    private final int monthlyTokenAmount;
    private final List<Condition> conditions;
    private final List<Benefit> benefits;

    @Getter
    @Builder
    public static class Condition {
        private final String metricType;
        private final long threshold;
    }

    @Getter
    @Builder
    public static class Benefit {
        private final String benefitCode;
        private final String description;
    }
}
