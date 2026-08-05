package com.planwith.user.domain.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class RefreshTokenSession {

    private final String userId;
    /** SHA-256 hex hash of the opaque refresh token (never plaintext). */
    private final String tokenHash;
    private final String familyId;
    private final String sessionId;
    private final Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
