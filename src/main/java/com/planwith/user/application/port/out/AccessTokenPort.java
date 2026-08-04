package com.planwith.user.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface AccessTokenPort {

    IssuedAccessToken issue(AccessTokenCommand command);

    Map<String, Object> jwks();

    record AccessTokenCommand(
            String userId,
            List<String> roles,
            List<String> scopes,
            String sessionId
    ) {
    }

    record IssuedAccessToken(String token, Instant expiresAt, long expiresInSeconds) {
    }
}
