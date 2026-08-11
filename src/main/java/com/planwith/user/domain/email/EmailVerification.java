package com.planwith.user.domain.email;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmailVerification {

    private final Long id;
    private final String email;
    private final String code;
    private final boolean verified;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public static EmailVerification createPending(String email, String code, LocalDateTime expiresAt) {
        return EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matchesCode(String inputCode) {
        if (code == null || inputCode == null) {
            return false;
        }
        String expected = code.trim();
        String actual = inputCode.trim().replaceAll("\\s+", "");
        return expected.equals(actual);
    }

    public boolean isVerifiedWithin(LocalDateTime since) {
        return verified && createdAt != null && createdAt.isAfter(since);
    }
}
