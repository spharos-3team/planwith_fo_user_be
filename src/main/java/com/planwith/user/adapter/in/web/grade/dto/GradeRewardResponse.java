package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.GradeRewardView;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GradeRewardResponse {

    private final String memberUuid;
    private final String gradeCode;
    private final String rewardMonth;
    private final int tokenAmount;
    private final String rewardStatus;
    private final LocalDateTime createdAt;

    public static GradeRewardResponse from(GradeRewardView view) {
        return GradeRewardResponse.builder()
                .memberUuid(view.getMemberUuid())
                .gradeCode(view.getGradeCode())
                .rewardMonth(view.getRewardMonth())
                .tokenAmount(view.getTokenAmount())
                .rewardStatus(view.getRewardStatus())
                .createdAt(view.getCreatedAt())
                .build();
    }
}
