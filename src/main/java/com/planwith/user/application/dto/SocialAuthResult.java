package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialAuthResult {

    private final boolean needsSignup;
    private final TokenPair tokens;
    private final String provider;
    private final String email;
    private final String suggestedNickname;
}
