package com.planwith.user.application.port.in;

public interface FollowMemberUseCase {
    void follow(Long followerMemberId, String followeeMemberUuid);

    void unfollow(Long followerMemberId, String followeeMemberUuid);
}
