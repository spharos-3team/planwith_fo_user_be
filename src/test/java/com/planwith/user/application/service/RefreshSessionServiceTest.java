package com.planwith.user.application.service;

import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.out.RefreshTokenGeneratorPort;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.auth.RefreshTokenSession;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {

    @Mock private RefreshTokenGeneratorPort refreshTokenGeneratorPort;
    @Mock private RefreshTokenSessionPort refreshTokenSessionPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private TokenIssuanceService tokenIssuanceService;
    @InjectMocks private RefreshSessionService refreshSessionService;

    @Test
    @DisplayName("refresh rotates tokens when hash matches and not expired")
    void refresh_success() {
        User user = User.builder()
                .id(7L).email("u@e.com").nickname("n")
                .loginType(LoginType.LOCAL).status(UserStatus.ACTIVE).role("USER").build();
        TokenPair tokens = TokenPair.builder()
                .tokenType("Bearer").accessToken("new-a").accessTokenExpiresIn(900)
                .refreshToken("new-r")
                .user(TokenPair.UserInfo.builder().userId("7").roles(List.of("USER")).scopes(List.of()).build())
                .build();
        given(refreshTokenGeneratorPort.hash("raw-refresh")).willReturn("hash");
        given(refreshTokenSessionPort.findByTokenHash("hash")).willReturn(Optional.of(
                RefreshTokenSession.builder()
                        .userId("7").tokenHash("hash").familyId("family-1").sessionId("session-1")
                        .expiresAt(Instant.now().plusSeconds(3600)).build()));
        given(refreshTokenSessionPort.isFamilyCompromised("family-1")).willReturn(false);
        given(userRepositoryPort.findById(7L)).willReturn(Optional.of(user));
        given(tokenIssuanceService.rotateTokens(user, "family-1", "session-1")).willReturn(tokens);

        TokenPair result = refreshSessionService.refreshSession("raw-refresh");
        assertThat(result.getAccessToken()).isEqualTo("new-a");
        verify(refreshTokenSessionPort).deleteByTokenHash("hash");
        verify(tokenIssuanceService).rotateTokens(eq(user), eq("family-1"), eq("session-1"));
    }

    @Test
    @DisplayName("refresh fails when refresh token hash not found")
    void refresh_invalidToken() {
        given(refreshTokenGeneratorPort.hash("bad")).willReturn("hash");
        given(refreshTokenSessionPort.findByTokenHash("hash")).willReturn(Optional.empty());
        given(refreshTokenSessionPort.findUsedFamilyId("hash")).willReturn(Optional.empty());

        assertThatThrownBy(() -> refreshSessionService.refreshSession("bad"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("refresh detects reuse and compromises family")
    void refresh_reuseDetected() {
        given(refreshTokenGeneratorPort.hash("reused")).willReturn("used-hash");
        given(refreshTokenSessionPort.findByTokenHash("used-hash")).willReturn(Optional.empty());
        given(refreshTokenSessionPort.findUsedFamilyId("used-hash")).willReturn(Optional.of("family-x"));

        assertThatThrownBy(() -> refreshSessionService.refreshSession("reused"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        verify(refreshTokenSessionPort).markFamilyCompromised("family-x");
    }
}
