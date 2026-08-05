package com.planwith.user.application.service;

import com.planwith.user.application.dto.MemberProfileInfo;
import com.planwith.user.application.port.out.FollowPort;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private FollowPort followPort;
    @Mock private ProfanityFilterPort profanityFilterPort;
    @InjectMocks private MemberProfileService memberProfileService;

    @Test
    @DisplayName("getMyProfile returns email and counts")
    void getMyProfile_success() {
        User user = sampleUser(1L, "uuid-1", "nick", "a@b.com");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(user));
        given(followPort.countFollowers("uuid-1")).willReturn(3L);
        given(followPort.countFollowing("uuid-1")).willReturn(2L);

        MemberProfileInfo info = memberProfileService.getMyProfile(1L);

        assertThat(info.getEmail()).isEqualTo("a@b.com");
        assertThat(info.getMemberId()).isEqualTo(1L);
        assertThat(info.getFollowerCount()).isEqualTo(3L);
        assertThat(info.getFollowingCount()).isEqualTo(2L);
        assertThat(info.getFollowedByMe()).isNull();
    }

    @Test
    @DisplayName("getByMemberUuid sets followedByMe when viewer follows target")
    void getByMemberUuid_followedByMe() {
        User target = sampleUser(2L, "uuid-2", "other", "o@b.com");
        User viewer = sampleUser(1L, "uuid-1", "me", "a@b.com");
        given(userRepositoryPort.findActiveByMemberUuid("uuid-2")).willReturn(Optional.of(target));
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(viewer));
        given(followPort.find("uuid-1", "uuid-2"))
                .willReturn(Optional.of(new FollowPort.FollowRelation(1L, "f", "uuid-1", "uuid-2", true)));
        given(followPort.countFollowers("uuid-2")).willReturn(1L);
        given(followPort.countFollowing("uuid-2")).willReturn(0L);

        MemberProfileInfo info = memberProfileService.getByMemberUuid("uuid-2", 1L);

        assertThat(info.getEmail()).isNull();
        assertThat(info.getFollowedByMe()).isTrue();
    }

    @Test
    @DisplayName("updateMyProfile rejects duplicated nickname")
    void updateMyProfile_nicknameDuplicated() {
        User user = sampleUser(1L, "uuid-1", "nick", "a@b.com");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(user));
        given(userRepositoryPort.existsActiveByNicknameExcludingMemberId("taken", 1L)).willReturn(true);

        assertThatThrownBy(() -> memberProfileService.updateMyProfile(1L, "taken", null, null))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_DUPLICATED);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("updateMyProfile updates nickname and intro")
    void updateMyProfile_success() {
        User user = sampleUser(1L, "uuid-1", "nick", "a@b.com");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(user));
        given(userRepositoryPort.existsActiveByNicknameExcludingMemberId("newNick", 1L)).willReturn(false);
        given(userRepositoryPort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(followPort.countFollowers("uuid-1")).willReturn(0L);
        given(followPort.countFollowing("uuid-1")).willReturn(0L);

        MemberProfileInfo info = memberProfileService.updateMyProfile(1L, "newNick", "/img.png", "hello");

        assertThat(info.getNickname()).isEqualTo("newNick");
        assertThat(info.getProfileImage()).isEqualTo("/img.png");
        assertThat(info.getProfileIntro()).isEqualTo("hello");
        verify(profanityFilterPort).validate("newNick");
        verify(profanityFilterPort).validate("hello");
    }

    private static User sampleUser(Long id, String uuid, String nickname, String email) {
        return User.builder()
                .id(id)
                .memberUuid(uuid)
                .email(email)
                .nickname(nickname)
                .loginType(LoginType.LOCAL)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
    }
}
