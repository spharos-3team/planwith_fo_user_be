package com.planwith.user.adapter.out.persistence.mapper;

import com.planwith.user.adapter.out.persistence.entity.UserJpaEntity;
import com.planwith.user.domain.user.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .gradeId(entity.getGradeId())
                .followId(entity.getFollowId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .nickname(entity.getNickname())
                .profileImage(entity.getProfileImage())
                .introduction(entity.getIntroduction())
                .loginType(entity.getLoginType())
                .providerId(entity.getProviderId())
                .status(entity.getStatus())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .gradeId(user.getGradeId())
                .followId(user.getFollowId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .loginType(user.getLoginType())
                .providerId(user.getProviderId())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
