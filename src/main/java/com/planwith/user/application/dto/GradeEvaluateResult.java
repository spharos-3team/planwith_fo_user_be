package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GradeEvaluateResult {

    private final String memberUuid;
    private final String previousGradeCode;
    private final String currentGradeCode;
    private final boolean upgraded;
    private final List<MemberGradeView.Metric> metrics;
}
