package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.SocialAuthResult;

public interface SocialLoginUseCase {

    SocialAuthResult socialLogin(
            String provider,
            String accessToken,
            String authorizationCode,
            String redirectUri,
            String state
    );
}
