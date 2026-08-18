package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.GradeEvaluateResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradeEvaluateResponse {

    private final String memberUuid;
    private final String previousGradeCode;
    private final String currentGradeCode;
    private final boolean upgraded;
    private final List<MemberGradeResponse.MetricResponse> metrics;

    public static GradeEvaluateResponse from(GradeEvaluateResult result) {
        return GradeEvaluateResponse.builder()
                .memberUuid(result.getMemberUuid())
                .previousGradeCode(result.getPreviousGradeCode())
                .currentGradeCode(result.getCurrentGradeCode())
                .upgraded(result.isUpgraded())
                .metrics(result.getMetrics().stream()
                        .map(m -> new MemberGradeResponse.MetricResponse(
                                m.getMetricType(),
                                m.getCurrentValue(),
                                m.getSourceService(),
                                m.getSourceVersion(),
                                m.getSynchronizedAt()
                        ))
                        .toList())
                .build();
    }
}
