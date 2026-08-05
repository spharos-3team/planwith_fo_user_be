package com.planwith.user.adapter.out.persistence.mapper;

import com.planwith.user.adapter.out.persistence.entity.MemberAuthJpaEntity;
import com.planwith.user.adapter.out.persistence.entity.MemberJpaEntity;
import com.planwith.user.adapter.out.persistence.entity.MemberProfileJpaEntity;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(MemberJpaEntity member, MemberAuthJpaEntity auth, MemberProfileJpaEntity profile) {
        return User.builder()
                .id(member.getMemberId())
                .memberUuid(member.getMemberUuid())
                .grade(profile != null ? profile.getGrade() : null)
                .email(auth != null ? auth.getEmail() : null)
                .password(auth != null ? auth.getPassword() : null)
                .nickname(profile != null ? profile.getNickname() : null)
                .profileImage(profile != null ? profile.getProfileImage() : null)
                .introduction(profile != null ? profile.getProfileIntro() : null)
                .loginType(auth != null ? auth.getLoginType() : null)
                .providerId(auth != null ? auth.getSocialId() : null)
                .status(member.getStatus())
                .role("USER")
                .lastLoginAt(auth != null ? auth.getLastLoginAt() : null)
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .deletedAt(member.getDeletedAt())
                .build();
    }

    public static MemberJpaEntity toNewMemberEntity(User user) {
        return MemberJpaEntity.builder()
                .memberId(user.getId())
                .memberUuid(user.getMemberUuid())
                .status(user.getStatus() != null ? user.getStatus() : UserStatus.ACTIVE)
                .deletedAt(user.getDeletedAt())
                .build();
    }

    public static MemberAuthJpaEntity toNewAuthEntity(Long memberId, User user) {
        return MemberAuthJpaEntity.builder()
                .memberId(memberId)
                .loginType(user.getLoginType())
                .email(user.getEmail())
                .password(user.getPassword())
                .socialId(user.getProviderId())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public static MemberProfileJpaEntity toNewProfileEntity(Long memberId, User user) {
        return MemberProfileJpaEntity.builder()
                .memberId(memberId)
                .memberUuid(user.getMemberUuid())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .profileIntro(user.getIntroduction())
                .grade(user.getGrade())
                .build();
    }
}
