package com.planwith.user.adapter.in.web.member.dto;

import com.planwith.user.application.dto.MemberProfileInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponse {

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

    public static MemberProfileResponse from(MemberProfileInfo info) {
        return MemberProfileResponse.builder()
                .memberId(info.getMemberId())
                .memberUuid(info.getMemberUuid())
                .nickname(info.getNickname())
                .profileImage(info.getProfileImage())
                .profileIntro(info.getProfileIntro())
                .grade(info.getGrade())
                .email(info.getEmail())
                .followerCount(info.getFollowerCount())
                .followingCount(info.getFollowingCount())
                .followedByMe(info.getFollowedByMe())
                .build();
    }
}
