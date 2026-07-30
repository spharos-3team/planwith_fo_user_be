package com.planwith.user.adapter.in.web.auth;

import com.planwith.user.adapter.in.gateway.GatewayAuthenticationContextResolver;
import com.planwith.user.adapter.in.gateway.GatewayTrustInterceptor;
import com.planwith.user.adapter.in.web.config.WebMvcConfig;
import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.in.CheckEmailDuplicationUseCase;
import com.planwith.user.application.port.in.CheckNicknameDuplicationUseCase;
import com.planwith.user.application.port.in.LoginUseCase;
import com.planwith.user.application.port.in.LogoutUseCase;
import com.planwith.user.application.port.in.RefreshSessionUseCase;
import com.planwith.user.application.port.in.ResetPasswordUseCase;
import com.planwith.user.application.port.in.SendEmailVerificationUseCase;
import com.planwith.user.application.port.in.SignUpUseCase;
import com.planwith.user.application.port.in.SocialLoginUseCase;
import com.planwith.user.application.port.in.SocialSignUpUseCase;
import com.planwith.user.application.port.in.UploadProfileImageUseCase;
import com.planwith.user.application.port.in.VerifyEmailCodeUseCase;
import com.planwith.user.application.port.in.WithdrawUseCase;
import com.planwith.user.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
@Import({
        GlobalExceptionHandler.class,
        WebMvcConfig.class,
        RefreshTokenCookieWriter.class,
        GatewayTrustInterceptor.class,
        GatewayAuthenticationContextResolver.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CheckEmailDuplicationUseCase checkEmailDuplicationUseCase;
    @MockBean private CheckNicknameDuplicationUseCase checkNicknameDuplicationUseCase;
    @MockBean private SendEmailVerificationUseCase sendEmailVerificationUseCase;
    @MockBean private VerifyEmailCodeUseCase verifyEmailCodeUseCase;
    @MockBean private UploadProfileImageUseCase uploadProfileImageUseCase;
    @MockBean private SignUpUseCase signUpUseCase;
    @MockBean private LoginUseCase loginUseCase;
    @MockBean private SocialLoginUseCase socialLoginUseCase;
    @MockBean private SocialSignUpUseCase socialSignUpUseCase;
    @MockBean private RefreshSessionUseCase refreshSessionUseCase;
    @MockBean private LogoutUseCase logoutUseCase;
    @MockBean private ResetPasswordUseCase resetPasswordUseCase;
    @MockBean private WithdrawUseCase withdrawUseCase;

    @Test
    @DisplayName("POST /api/v1/auth/login returns TokenResponse without refresh token in body")
    void login_success() throws Exception {
        given(loginUseCase.login("a@b.com", "Passw0rd!")).willReturn(sampleTokens("access", "refresh-secret"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Gateway-Internal-Token", "test-gateway-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@b.com","password":"Passw0rd!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").value("access"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900))
                .andExpect(jsonPath("$.data.user.userId").value("1"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login rejects invalid body")
    void login_validationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Gateway-Internal-Token", "test-gateway-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh uses HttpOnly cookie and returns same TokenResponse shape")
    void refresh_success() throws Exception {
        given(refreshSessionUseCase.refreshSession("refresh-secret"))
                .willReturn(sampleTokens("new-access", "new-refresh"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("X-Gateway-Internal-Token", "test-gateway-token")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/email/verify delegates to use case")
    void verifyEmail_delegates() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email/verify")
                        .header("X-Gateway-Internal-Token", "test-gateway-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@b.com","code":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(verifyEmailCodeUseCase).verifyCode("a@b.com", "123456");
    }

    @Test
    @DisplayName("Protected auth endpoint rejects missing gateway trust token")
    void gatewayTrust_required() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@b.com","password":"Passw0rd!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("GATEWAY_TRUST_FAILED"));
    }

    private static TokenPair sampleTokens(String access, String refresh) {
        return TokenPair.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .accessTokenExpiresIn(900)
                .refreshToken(refresh)
                .user(TokenPair.UserInfo.builder()
                        .userId("1")
                        .roles(List.of("USER"))
                        .scopes(List.of("profile:read", "plan:read"))
                        .build())
                .build();
    }
}
