package com.planwith.user.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("withdraw anonymizes personal fields and marks deleted")
    void withdraw_anonymizesAndDeletes() {
        User user = User.builder()
                .id(10L)
                .email("user@example.com")
                .password("encoded")
                .nickname("닉네임")
                .profileImage("/files/a.jpg")
                .introduction("소개")
                .providerId("provider-1")
                .loginType(LoginType.LOCAL)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();

        user.withdraw();

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getEmail()).startsWith("deleted_10_").endsWith("@withdrawn.local");
        assertThat(user.getNickname()).isEqualTo("탈퇴회원_10");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getProfileImage()).isNull();
        assertThat(user.getIntroduction()).isNull();
        assertThat(user.getProviderId()).isNull();
    }

    @Test
    @DisplayName("changePassword updates encoded password")
    void changePassword_updatesPassword() {
        User user = User.createLocal(1L, "a@b.com", "old", "nick", null, null);

        user.changePassword("new-encoded");

        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }
}
