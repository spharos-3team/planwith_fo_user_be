package com.planwith.user.application.service;

import com.planwith.user.application.port.out.SocialOAuthTokenPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SocialAccessTokenResolver {

    private final SocialOAuthTokenPort socialOAuthTokenPort;

    public String resolve(LoginType loginType, String accessToken, String authorizationCode, String redirectUri, String state) {
        if (StringUtils.hasText(accessToken)) {
            return accessToken.trim();
        }
        if (!StringUtils.hasText(authorizationCode) || !StringUtils.hasText(redirectUri)) {
            throw new CustomException(ErrorCode.SOCIAL_CREDENTIAL_REQUIRED);
        }
        return socialOAuthTokenPort.exchangeAuthorizationCode(
                loginType,
                authorizationCode.trim(),
                redirectUri.trim(),
                StringUtils.hasText(state) ? state.trim() : null
        );
    }
}
