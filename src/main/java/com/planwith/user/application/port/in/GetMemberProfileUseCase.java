package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.MemberProfileInfo;

public interface GetMemberProfileUseCase {
    MemberProfileInfo getByMemberUuid(String memberUuid, Long viewerMemberIdOrNull);

    MemberProfileInfo getByNickname(String nickname, Long viewerMemberIdOrNull);
}
