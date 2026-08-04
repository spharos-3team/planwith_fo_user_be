package com.planwith.user.adapter.out.security;

import com.planwith.user.global.config.SecurityCryptoConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderAdapterTest {

    @Test
    @DisplayName("delegating password encoder hashes and matches without storing plaintext")
    void passwordHash_roundTrip() {
        PasswordEncoder encoder = new SecurityCryptoConfig().passwordEncoder();
        PasswordEncoderAdapter adapter = new PasswordEncoderAdapter(encoder);

        String encoded = adapter.encode("Passw0rd!");
        assertThat(encoded).isNotEqualTo("Passw0rd!");
        assertThat(encoded).startsWith("{");
        assertThat(adapter.matches("Passw0rd!", encoded)).isTrue();
        assertThat(adapter.matches("wrong", encoded)).isFalse();
    }
}
