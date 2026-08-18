package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TokenPair {

    private final String tokenType;
    private final String accessToken;
    private final long accessTokenExpiresIn;
    private final String refreshToken;
    private final UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private final String userId;
        private final List<String> roles;
        private final List<String> scopes;
    }
}
