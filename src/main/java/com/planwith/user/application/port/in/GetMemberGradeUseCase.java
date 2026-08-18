package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.MemberGradeView;

public interface GetMemberGradeUseCase {
    MemberGradeView getMyGrade(Long memberId);

    MemberGradeView getByMemberUuid(String memberUuid);
}
