package com.planwith.user.application.service;

import com.planwith.user.application.dto.SocialAuthResult;
import com.planwith.user.application.port.in.SocialLoginUseCase;
import com.planwith.user.application.port.out.SocialUserInfoPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLoginService implements SocialLoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final SocialUserInfoPort socialUserInfoPort;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    @Transactional
    public SocialAuthResult socialLogin(String provider, String accessToken) {
        LoginType loginType = parseLoginType(provider);
        SocialUserInfoPort.SocialUserInfo socialUserInfo = socialUserInfoPort.getUserInfo(loginType, accessToken);

        return userRepositoryPort
                .findActiveByLoginTypeAndProviderId(loginType, socialUserInfo.getProviderId())
                .map(user -> SocialAuthResult.builder()
                        .needsSignup(false)
                        .tokens(tokenIssuanceService.issueTokens(user))
                        .build())
                .orElseGet(() -> SocialAuthResult.builder()
                        .needsSignup(true)
                        .provider(provider.toUpperCase())
                        .email(socialUserInfo.getEmail())
                        .suggestedNickname(socialUserInfo.getNickname())
                        .build());
    }

    private LoginType parseLoginType(String provider) {
        try {
            return LoginType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
