package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.GradeRewardView;

import java.util.List;

public interface ListGradeRewardsUseCase {
    List<GradeRewardView> listMyRewards(Long memberId);
}
