package com.planwith.user.adapter.in.web.sse;

import java.time.Instant;

public record SseEventPayload<T>(
        String eventId,
        Instant occurredAt,
        T payload
) {

    public static <T> SseEventPayload<T> of(String eventId, T payload) {
        return new SseEventPayload<>(eventId, Instant.now(), payload);
    }
}
