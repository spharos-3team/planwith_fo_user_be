package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.GradeEvaluateResult;

public interface EvaluateMemberGradeUseCase {
    GradeEvaluateResult evaluate(String memberUuid, long storyCount, long likeCount);
}
