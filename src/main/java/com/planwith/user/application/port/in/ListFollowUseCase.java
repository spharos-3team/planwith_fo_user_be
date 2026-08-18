package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.MemberProfileInfo;

import java.util.List;

public interface ListFollowUseCase {
    List<MemberProfileInfo> listFollowers(String memberUuid);

    List<MemberProfileInfo> listFollowing(String memberUuid);
}
