package com.planwith.user.adapter.out.redis;

import com.planwith.user.domain.auth.RefreshTokenSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRefreshTokenSessionAdapterTest {

    private final InMemoryRefreshTokenSessionAdapter adapter = new InMemoryRefreshTokenSessionAdapter();

    @Test
    @DisplayName("rotation marks old hash used and reuse can be detected")
    void rotationAndReuse() {
        RefreshTokenSession first = RefreshTokenSession.builder()
                .userId("1").tokenHash("hash-1").familyId("fam").sessionId("sess")
                .expiresAt(Instant.now().plusSeconds(3600)).build();
        adapter.save(first);
        adapter.deleteByTokenHash("hash-1");

        assertThat(adapter.findByTokenHash("hash-1")).isEmpty();
        assertThat(adapter.findUsedFamilyId("hash-1")).contains("fam");

        adapter.markFamilyCompromised("fam");
        assertThat(adapter.isFamilyCompromised("fam")).isTrue();
    }
}
