package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.GradeCatalogItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradeCatalogResponse {

    private final String gradeCode;
    private final String nameKo;
    private final int sortOrder;
    private final int monthlyTokenAmount;
    private final List<ConditionResponse> conditions;
    private final List<BenefitResponse> benefits;

    public static GradeCatalogResponse from(GradeCatalogItem item) {
        return GradeCatalogResponse.builder()
                .gradeCode(item.getGradeCode())
                .nameKo(item.getNameKo())
                .sortOrder(item.getSortOrder())
                .monthlyTokenAmount(item.getMonthlyTokenAmount())
                .conditions(item.getConditions().stream()
                        .map(c -> new ConditionResponse(c.getMetricType(), c.getThreshold()))
                        .toList())
                .benefits(item.getBenefits().stream()
                        .map(b -> new BenefitResponse(b.getBenefitCode(), b.getDescription()))
                        .toList())
                .build();
    }

    public record ConditionResponse(String metricType, long threshold) {
    }

    public record BenefitResponse(String benefitCode, String description) {
    }
}
