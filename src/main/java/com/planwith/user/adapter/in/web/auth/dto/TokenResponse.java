package com.planwith.user.adapter.in.web.auth.dto;

import com.planwith.user.application.dto.TokenPair;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TokenResponse {

    private final String tokenType;
    private final String accessToken;
    private final long accessTokenExpiresIn;
    private final UserPayload user;

    public static TokenResponse from(TokenPair tokenPair) {
        return TokenResponse.builder()
                .tokenType(tokenPair.getTokenType())
                .accessToken(tokenPair.getAccessToken())
                .accessTokenExpiresIn(tokenPair.getAccessTokenExpiresIn())
                .user(UserPayload.builder()
                        .userId(tokenPair.getUser().getUserId())
                        .roles(tokenPair.getUser().getRoles())
                        .scopes(tokenPair.getUser().getScopes())
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class UserPayload {
        private final String userId;
        private final List<String> roles;
        private final List<String> scopes;
    }
}
