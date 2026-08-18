package com.planwith.user.application.service;

import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.out.AccessTokenPort;
import com.planwith.user.application.port.out.RefreshTokenGeneratorPort;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.application.port.out.TokenExpirePropertiesPort;
import com.planwith.user.domain.auth.RefreshTokenSession;
import com.planwith.user.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class TokenIssuanceService {

    private static final List<String> DEFAULT_SCOPES = List.of("profile:read", "plan:read");

    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenGeneratorPort refreshTokenGeneratorPort;
    private final RefreshTokenSessionPort refreshTokenSessionPort;
    private final TokenExpirePropertiesPort tokenExpirePropertiesPort;

    TokenPair issueTokens(User user) {
        return issueTokens(user, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    TokenPair rotateTokens(User user, String familyId, String sessionId) {
        return issueTokens(user, familyId, sessionId);
    }

    private TokenPair issueTokens(User user, String familyId, String sessionId) {
        String userId = String.valueOf(user.getId());
        List<String> roles = List.of(user.getRole() == null ? "USER" : user.getRole());

        AccessTokenPort.IssuedAccessToken accessToken = accessTokenPort.issue(
                new AccessTokenPort.AccessTokenCommand(userId, roles, DEFAULT_SCOPES, sessionId));
        RefreshTokenGeneratorPort.GeneratedRefreshToken refreshToken = refreshTokenGeneratorPort.generate();

        Instant expiresAt = Instant.now().plusMillis(tokenExpirePropertiesPort.getRefreshTokenExpireMs());
        refreshTokenSessionPort.save(RefreshTokenSession.builder()
                .userId(userId)
                .tokenHash(refreshToken.getTokenHash())
                .familyId(familyId)
                .sessionId(sessionId)
                .expiresAt(expiresAt)
                .build());

        return TokenPair.builder()
                .tokenType("Bearer")
                .accessToken(accessToken.token())
                .accessTokenExpiresIn(accessToken.expiresInSeconds())
                .refreshToken(refreshToken.getRawToken())
                .user(TokenPair.UserInfo.builder()
                        .userId(userId)
                        .roles(roles)
                        .scopes(DEFAULT_SCOPES)
                        .build())
                .build();
    }
}
