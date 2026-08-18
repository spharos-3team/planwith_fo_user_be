package com.planwith.user.application.port.out;

import lombok.Getter;

public interface RefreshTokenGeneratorPort {

    GeneratedRefreshToken generate();

    String hash(String rawToken);

    @Getter
    class GeneratedRefreshToken {
        private final String rawToken;
        private final String tokenHash;

        public GeneratedRefreshToken(String rawToken, String tokenHash) {
            this.rawToken = rawToken;
            this.tokenHash = tokenHash;
        }
    }
}
