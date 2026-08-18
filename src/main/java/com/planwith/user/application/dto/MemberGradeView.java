package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemberGradeView {

    private final String memberUuid;
    private final String gradeUuid;
    private final String gradeCode;
    private final String gradeName;
    private final int gradeLevel;
    private final String gradeStatus;
    private final LocalDateTime gradeAssignedAt;
    private final LocalDateTime lastEvaluatedAt;
    private final List<Metric> metrics;
    private final List<GradeCatalogItem.Benefit> benefits;

    @Getter
    @Builder
    public static class Metric {
        private final String metricType;
        private final long currentValue;
        private final String sourceService;
        private final long sourceVersion;
        private final LocalDateTime synchronizedAt;
    }
}
