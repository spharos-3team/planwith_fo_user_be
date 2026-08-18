package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.GradeCatalogItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradeCatalogResponse {

    private final String gradeCode;
    private final String gradeName;
    private final int gradeLevel;
    private final String description;
    private final List<ConditionResponse> conditions;
    private final List<BenefitResponse> benefits;

    public static GradeCatalogResponse from(GradeCatalogItem item) {
        return GradeCatalogResponse.builder()
                .gradeCode(item.getGradeCode())
                .gradeName(item.getGradeName())
                .gradeLevel(item.getGradeLevel())
                .description(item.getDescription())
                .conditions(item.getConditions().stream()
                        .map(c -> new ConditionResponse(
                                c.getMetricType(),
                                c.getConditionName(),
                                c.getThresholdValue(),
                                c.getSortOrder(),
                                c.getDescription()
                        ))
                        .toList())
                .benefits(item.getBenefits().stream()
                        .map(BenefitResponse::from)
                        .toList())
                .build();
    }

    public record ConditionResponse(
            String metricType,
            String conditionName,
            long thresholdValue,
            int sortOrder,
            String description
    ) {
    }

    public record BenefitResponse(
            String benefitCode,
            String benefitName,
            String benefitValue,
            String description,
            int sortOrder
    ) {
        public static BenefitResponse from(GradeCatalogItem.Benefit b) {
            return new BenefitResponse(
                    b.getBenefitCode(),
                    b.getBenefitName(),
                    b.getBenefitValue(),
                    b.getDescription(),
                    b.getSortOrder()
            );
        }
    }
}
