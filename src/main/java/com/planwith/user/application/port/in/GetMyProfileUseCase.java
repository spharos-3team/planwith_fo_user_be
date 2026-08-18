package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.MemberProfileInfo;

public interface GetMyProfileUseCase {
    MemberProfileInfo getMyProfile(Long memberId);
}
