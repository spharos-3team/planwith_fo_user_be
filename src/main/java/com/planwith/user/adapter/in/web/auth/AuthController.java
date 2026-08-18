package com.planwith.user.adapter.in.web.auth;

import com.planwith.user.adapter.in.gateway.AuthenticatedUserContext;
import com.planwith.user.adapter.in.gateway.GatewayAuthenticationContextResolver;
import com.planwith.user.adapter.in.web.auth.dto.*;
import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.in.*;
import com.planwith.user.global.common.ApiResponse;
import com.planwith.user.global.config.AppProperties;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CheckEmailDuplicationUseCase checkEmailDuplicationUseCase;
    private final CheckNicknameDuplicationUseCase checkNicknameDuplicationUseCase;
    private final SendEmailVerificationUseCase sendEmailVerificationUseCase;
    private final VerifyEmailCodeUseCase verifyEmailCodeUseCase;
    private final UploadProfileImageUseCase uploadProfileImageUseCase;
    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final SocialLoginUseCase socialLoginUseCase;
    private final SocialSignUpUseCase socialSignUpUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final RefreshTokenCookieWriter refreshTokenCookieWriter;
    private final AppProperties appProperties;

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(checkEmailDuplicationUseCase.isEmailDuplicated(email)));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(ApiResponse.success(checkNicknameDuplicationUseCase.isNicknameDuplicated(nickname)));
    }

    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmailCode(@Valid @RequestBody EmailSendRequest request) {
        sendEmailVerificationUseCase.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailCode(@Valid @RequestBody EmailVerifyRequest request) {
        verifyEmailCodeUseCase.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping(value = "/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String url = uploadProfileImageUseCase.upload(bytes, file.getContentType(), file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.success(url));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        signUpUseCase.signUp(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getProfileImage(),
                request.getIntroduction(),
                request.getAgreedTermIds()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokens = loginUseCase.login(request.getEmail(), request.getPassword());
        writeRefreshCookie(response, tokens);
        return ResponseEntity.ok(ApiResponse.success(TokenResponse.from(tokens)));
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<SocialAuthResponse>> socialLogin(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletResponse response
    ) {
        var result = socialLoginUseCase.socialLogin(
                request.getProvider(),
                request.getAccessToken(),
                request.getAuthorizationCode(),
                request.getRedirectUri(),
                request.getState());
        if (result.getTokens() != null) {
            writeRefreshCookie(response, result.getTokens());
        }
        return ResponseEntity.ok(ApiResponse.success(SocialAuthResponse.from(result)));
    }

    @PostMapping("/social-signup")
    public ResponseEntity<ApiResponse<TokenResponse>> socialSignUp(
            @Valid @RequestBody SocialSignUpRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokens = socialSignUpUseCase.socialSignUp(
                request.getProvider(),
                request.getAccessToken(),
                request.getAuthorizationCode(),
                request.getRedirectUri(),
                request.getState(),
                request.getNickname(),
                request.getAgreedTermIds());
        writeRefreshCookie(response, tokens);
        return ResponseEntity.ok(ApiResponse.success(TokenResponse.from(tokens)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = refreshTokenCookieWriter.read(request)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
        TokenPair tokens = refreshSessionUseCase.refreshSession(refreshToken);
        writeRefreshCookie(response, tokens);
        return ResponseEntity.ok(ApiResponse.success(TokenResponse.from(tokens)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        refreshTokenCookieWriter.read(request).ifPresent(logoutUseCase::logout);
        refreshTokenCookieWriter.clear(response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            AuthenticatedUserContext userContext,
            HttpServletResponse response
    ) {
        AuthenticatedUserContext authenticated = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        logoutUseCase.logoutAll(authenticated.userId());
        refreshTokenCookieWriter.clear(response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        resetPasswordUseCase.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            AuthenticatedUserContext userContext,
            @Valid @RequestBody WithdrawRequest request
    ) {
        AuthenticatedUserContext authenticated = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        withdrawUseCase.withdraw(Long.valueOf(authenticated.userId()), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void writeRefreshCookie(HttpServletResponse response, TokenPair tokens) {
        refreshTokenCookieWriter.write(
                response,
                tokens.getRefreshToken(),
                appProperties.getJwt().getRefreshTokenTtl()
        );
    }
}
