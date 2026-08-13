package com.planwith.user.application.service;

import com.planwith.user.application.dto.SocialAuthResult;
import com.planwith.user.application.port.in.SocialLoginUseCase;
import com.planwith.user.application.port.out.SocialUserInfoPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLoginService implements SocialLoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final SocialUserInfoPort socialUserInfoPort;
    private final SocialAccessTokenResolver socialAccessTokenResolver;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    @Transactional
    public SocialAuthResult socialLogin(
            String provider,
            String accessToken,
            String authorizationCode,
            String redirectUri,
            String state
    ) {
        LoginType loginType = parseLoginType(provider);
        String resolvedToken = socialAccessTokenResolver.resolve(
                loginType, accessToken, authorizationCode, redirectUri, state);
        SocialUserInfoPort.SocialUserInfo socialUserInfo = socialUserInfoPort.getUserInfo(loginType, resolvedToken);

        var existing = userRepositoryPort.findByLoginTypeAndProviderId(loginType, socialUserInfo.getProviderId());
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.isSuspended()) {
                throw new CustomException(ErrorCode.ACCOUNT_SUSPENDED);
            }
            if (!user.isActive()) {
                throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
            }
            user.recordLastLogin(LocalDateTime.now());
            User saved = userRepositoryPort.save(user);
            return SocialAuthResult.builder()
                    .needsSignup(false)
                    .tokens(tokenIssuanceService.issueTokens(saved))
                    .build();
        }

        return SocialAuthResult.builder()
                .needsSignup(true)
                .provider(provider.toUpperCase())
                .email(socialUserInfo.getEmail())
                .suggestedNickname(socialUserInfo.getNickname())
                // code is one-time; client must use this token for social-signup
                .providerAccessToken(resolvedToken)
                .build();
    }

    private LoginType parseLoginType(String provider) {
        try {
            return LoginType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
