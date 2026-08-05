package com.planwith.user.application.service;

import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.out.PasswordEncoderPort;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private PasswordEncoderPort passwordEncoderPort;
    @Mock private TokenIssuanceService tokenIssuanceService;
    @InjectMocks private LoginService loginService;

    @Test
    @DisplayName("login issues tokens for valid local credentials")
    void login_success() {
        User user = User.builder()
                .id(1L).email("a@b.com").password("encoded").nickname("nick")
                .loginType(LoginType.LOCAL).status(UserStatus.ACTIVE).role("USER").build();
        TokenPair tokens = TokenPair.builder()
                .tokenType("Bearer")
                .accessToken("a")
                .accessTokenExpiresIn(900)
                .refreshToken("r")
                .user(TokenPair.UserInfo.builder()
                        .userId("1").roles(List.of("USER")).scopes(List.of("profile:read")).build())
                .build();
        given(userRepositoryPort.findActiveByEmail("a@b.com")).willReturn(Optional.of(user));
        given(passwordEncoderPort.matches("Passw0rd!", "encoded")).willReturn(true);
        given(userRepositoryPort.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(tokenIssuanceService.issueTokens(any(User.class))).willReturn(tokens);

        TokenPair result = loginService.login("a@b.com", "Passw0rd!");
        assertThat(result.getAccessToken()).isEqualTo("a");
        verify(userRepositoryPort).save(any(User.class));
        verify(tokenIssuanceService).issueTokens(any(User.class));
    }

    @Test
    @DisplayName("login rejects wrong password")
    void login_invalidPassword() {
        User user = User.builder()
                .id(1L).email("a@b.com").password("encoded").nickname("nick")
                .loginType(LoginType.LOCAL).status(UserStatus.ACTIVE).role("USER").build();
        given(userRepositoryPort.findActiveByEmail("a@b.com")).willReturn(Optional.of(user));
        given(passwordEncoderPort.matches("bad", "encoded")).willReturn(false);

        assertThatThrownBy(() -> loginService.login("a@b.com", "bad"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }
}
