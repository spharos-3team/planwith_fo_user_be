package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradeEvaluateResult {

    private final String memberUuid;
    private final String previousGradeCode;
    private final String currentGradeCode;
    private final boolean upgraded;
    private final MemberGradeView.Metrics metrics;
}
