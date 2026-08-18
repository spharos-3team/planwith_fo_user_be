package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileInfo {

    private final Long memberId;
    private final String memberUuid;
    private final String nickname;
    private final String profileImage;
    private final String profileIntro;
    private final String grade;
    private final String email;
    private final long followerCount;
    private final long followingCount;
    private final Boolean followedByMe;
}
