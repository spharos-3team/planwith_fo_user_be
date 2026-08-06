package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GradeRewardView {

    private final String memberUuid;
    private final String gradeCode;
    private final String rewardMonth;
    private final int tokenAmount;
    private final String rewardStatus;
    private final LocalDateTime createdAt;
}
