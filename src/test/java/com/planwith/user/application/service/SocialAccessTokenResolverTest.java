package com.planwith.user.application.service;

import com.planwith.user.application.port.out.SocialOAuthTokenPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SocialAccessTokenResolverTest {

    @Mock
    private SocialOAuthTokenPort socialOAuthTokenPort;

    @InjectMocks
    private SocialAccessTokenResolver resolver;

    @Test
    @DisplayName("prefers accessToken when present")
    void prefersAccessToken() {
        String token = resolver.resolve(LoginType.GOOGLE, " access ", "code", "https://app/cb", null);
        assertThat(token).isEqualTo("access");
        verifyNoInteractions(socialOAuthTokenPort);
    }

    @Test
    @DisplayName("exchanges authorization code when accessToken missing")
    void exchangesCode() {
        given(socialOAuthTokenPort.exchangeAuthorizationCode(
                LoginType.KAKAO, "auth-code", "https://app/cb", "state-1"))
                .willReturn("exchanged-token");

        String token = resolver.resolve(LoginType.KAKAO, null, "auth-code", "https://app/cb", "state-1");
        assertThat(token).isEqualTo("exchanged-token");
    }

    @Test
    @DisplayName("requires code and redirectUri together")
    void requiresCodePair() {
        assertThatThrownBy(() -> resolver.resolve(LoginType.NAVER, null, "only-code", null, null))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SOCIAL_CREDENTIAL_REQUIRED);
    }
}
