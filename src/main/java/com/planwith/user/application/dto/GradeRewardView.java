package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GradeRewardView {

    private final String rewardUuid;
    private final String gradeCode;
    private final String rewardType;
    private final int amount;
    private final String periodYm;
    private final LocalDateTime grantedAt;
}
