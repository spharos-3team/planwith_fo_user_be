package com.planwith.user.adapter.in.gateway;

import java.util.List;

public record AuthenticatedUserContext(
        String userId,
        List<String> roles,
        List<String> scopes,
        String sessionId,
        String requestId
) {

    public static AuthenticatedUserContext anonymous(String requestId) {
        return new AuthenticatedUserContext(null, List.of(), List.of(), null, requestId);
    }

    public boolean isAuthenticated() {
        return userId != null && !userId.isBlank();
    }
}
