package com.planwith.user.adapter.in.web.auth.dto;

import com.planwith.user.application.dto.SocialAuthResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialAuthResponse {

    private boolean needsSignup;
    private TokenResponse tokens;
    private String provider;
    private String email;
    private String suggestedNickname;
    /** Use this for social-signup; do not reuse authorizationCode. */
    private String providerAccessToken;

    public static SocialAuthResponse from(SocialAuthResult result) {
        return SocialAuthResponse.builder()
                .needsSignup(result.isNeedsSignup())
                .tokens(result.getTokens() != null ? TokenResponse.from(result.getTokens()) : null)
                .provider(result.getProvider())
                .email(result.getEmail())
                .suggestedNickname(result.getSuggestedNickname())
                .providerAccessToken(result.getProviderAccessToken())
                .build();
    }
}
