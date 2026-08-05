package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.GradeRewardView;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GradeRewardResponse {

    private final String rewardUuid;
    private final String gradeCode;
    private final String rewardType;
    private final int amount;
    private final String periodYm;
    private final LocalDateTime grantedAt;

    public static GradeRewardResponse from(GradeRewardView view) {
        return GradeRewardResponse.builder()
                .rewardUuid(view.getRewardUuid())
                .gradeCode(view.getGradeCode())
                .rewardType(view.getRewardType())
                .amount(view.getAmount())
                .periodYm(view.getPeriodYm())
                .grantedAt(view.getGrantedAt())
                .build();
    }
}
