package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.MemberGradeView;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemberGradeResponse {

    private final String memberUuid;
    private final String gradeUuid;
    private final String gradeCode;
    private final String gradeName;
    private final int gradeLevel;
    private final String gradeStatus;
    private final LocalDateTime gradeAssignedAt;
    private final LocalDateTime lastEvaluatedAt;
    private final List<MetricResponse> metrics;
    private final List<GradeCatalogResponse.BenefitResponse> benefits;

    public static MemberGradeResponse from(MemberGradeView view) {
        return MemberGradeResponse.builder()
                .memberUuid(view.getMemberUuid())
                .gradeUuid(view.getGradeUuid())
                .gradeCode(view.getGradeCode())
                .gradeName(view.getGradeName())
                .gradeLevel(view.getGradeLevel())
                .gradeStatus(view.getGradeStatus())
                .gradeAssignedAt(view.getGradeAssignedAt())
                .lastEvaluatedAt(view.getLastEvaluatedAt())
                .metrics(view.getMetrics().stream()
                        .map(m -> new MetricResponse(
                                m.getMetricType(),
                                m.getCurrentValue(),
                                m.getSourceService(),
                                m.getSourceVersion(),
                                m.getSynchronizedAt()
                        ))
                        .toList())
                .benefits(view.getBenefits().stream()
                        .map(GradeCatalogResponse.BenefitResponse::from)
                        .toList())
                .build();
    }

    public record MetricResponse(
            String metricType,
            long currentValue,
            String sourceService,
            long sourceVersion,
            LocalDateTime synchronizedAt
    ) {
    }
}
