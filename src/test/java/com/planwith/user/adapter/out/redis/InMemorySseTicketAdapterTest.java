package com.planwith.user.adapter.out.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemorySseTicketAdapterTest {

    private final InMemorySseTicketAdapter adapter = new InMemorySseTicketAdapter();

    @Test
    @DisplayName("SSE ticket is single-use")
    void ticket_singleUse() {
        adapter.save("ticket-1", "user-9", 30);
        assertThat(adapter.consume("ticket-1")).contains("user-9");
        assertThat(adapter.consume("ticket-1")).isEmpty();
    }
}
