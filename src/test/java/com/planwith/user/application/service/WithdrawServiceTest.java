package com.planwith.user.application.service;

import com.planwith.user.application.port.out.PasswordEncoderPort;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class WithdrawServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private PasswordEncoderPort passwordEncoderPort;
    @Mock private RefreshTokenSessionPort refreshTokenSessionPort;
    @InjectMocks private WithdrawService withdrawService;

    @Test
    @DisplayName("withdraw soft-deletes local member to DELETED and keeps row")
    void withdraw_local_softDelete() {
        User user = User.builder()
                .id(10L)
                .memberUuid("uuid-10")
                .email("a@b.com")
                .password("encoded")
                .nickname("nick")
                .loginType(LoginType.LOCAL)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
        given(userRepositoryPort.findById(10L)).willReturn(Optional.of(user));
        given(passwordEncoderPort.matches("Passw0rd!", "encoded")).willReturn(true);
        given(userRepositoryPort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        withdrawService.withdraw(10L, "Passw0rd!");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(saved.getDeletedAt()).isNotNull();
        assertThat(saved.getNickname()).isEqualTo("탈퇴회원_10");
        assertThat(saved.getPassword()).isNull();
        verify(refreshTokenSessionPort).deleteByUserId("10");
    }

    @Test
    @DisplayName("withdraw rejects wrong password for local account")
    void withdraw_invalidPassword() {
        User user = User.builder()
                .id(10L)
                .email("a@b.com")
                .password("encoded")
                .nickname("nick")
                .loginType(LoginType.LOCAL)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
        given(userRepositoryPort.findById(10L)).willReturn(Optional.of(user));
        given(passwordEncoderPort.matches("bad", "encoded")).willReturn(false);

        assertThatThrownBy(() -> withdrawService.withdraw(10L, "bad"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("withdraw allows social account without password")
    void withdraw_social_withoutPassword() {
        User user = User.builder()
                .id(11L)
                .memberUuid("uuid-11")
                .email("s@b.com")
                .nickname("social")
                .loginType(LoginType.KAKAO)
                .providerId("kakao-1")
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
        given(userRepositoryPort.findById(11L)).willReturn(Optional.of(user));
        given(userRepositoryPort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        withdrawService.withdraw(11L, null);

        verify(userRepositoryPort).save(any(User.class));
        verify(refreshTokenSessionPort).deleteByUserId("11");
    }

    @Test
    @DisplayName("withdraw rejects already deleted member")
    void withdraw_alreadyDeleted() {
        User user = User.builder()
                .id(10L)
                .status(UserStatus.DELETED)
                .loginType(LoginType.LOCAL)
                .password("encoded")
                .build();
        given(userRepositoryPort.findById(10L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> withdrawService.withdraw(10L, "Passw0rd!"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_WITHDRAWN);
    }
}
