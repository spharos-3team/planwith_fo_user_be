package com.planwith.user.domain.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTest {

    @Test
    @DisplayName("matchesCode and expiry checks work")
    void matchesAndExpiry() {
        EmailVerification verification = EmailVerification.builder()
                .email("a@b.com")
                .code("123456")
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(verification.matchesCode("123456")).isTrue();
        assertThat(verification.matchesCode("000000")).isFalse();
        assertThat(verification.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isVerifiedWithin requires verified flag and recent createdAt")
    void verifiedWithin() {
        EmailVerification verified = EmailVerification.builder()
                .email("a@b.com")
                .code("123456")
                .verified(true)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        assertThat(verified.isVerifiedWithin(LocalDateTime.now().minusMinutes(30))).isTrue();
        assertThat(verified.isVerifiedWithin(LocalDateTime.now().minusMinutes(5))).isFalse();
    }
}
