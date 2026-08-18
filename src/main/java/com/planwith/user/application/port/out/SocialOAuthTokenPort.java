package com.planwith.user.application.port.out;

import com.planwith.user.domain.user.LoginType;

public interface SocialOAuthTokenPort {

    /**
     * Exchange an authorization code for a provider access token.
     *
     * @param state optional; required by some providers (e.g. Naver) when the authorize step used state
     */
    String exchangeAuthorizationCode(LoginType loginType, String authorizationCode, String redirectUri, String state);
}
