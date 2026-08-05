package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.MemberProfileInfo;

public interface UpdateMyProfileUseCase {
    MemberProfileInfo updateMyProfile(Long memberId, String nickname, String profileImage, String profileIntro);
}
