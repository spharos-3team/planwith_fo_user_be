package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.GradeEvaluateResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradeEvaluateResponse {

    private final String memberUuid;
    private final String previousGradeCode;
    private final String currentGradeCode;
    private final boolean upgraded;
    private final MemberGradeResponse.MetricsResponse metrics;

    public static GradeEvaluateResponse from(GradeEvaluateResult result) {
        return GradeEvaluateResponse.builder()
                .memberUuid(result.getMemberUuid())
                .previousGradeCode(result.getPreviousGradeCode())
                .currentGradeCode(result.getCurrentGradeCode())
                .upgraded(result.isUpgraded())
                .metrics(new MemberGradeResponse.MetricsResponse(
                        result.getMetrics().getStoryCount(),
                        result.getMetrics().getFollowerCount(),
                        result.getMetrics().getLikeCount(),
                        result.getMetrics().getMetricsUpdatedAt()
                ))
                .build();
    }
}
