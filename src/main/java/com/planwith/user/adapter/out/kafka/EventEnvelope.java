package com.planwith.user.adapter.out.kafka;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class EventEnvelope<T> {

    private final String eventId;
    private final String eventType;
    private final Instant occurredAt;
    private final String aggregateId;
    private final long version;
    private final T payload;

    public static <T> EventEnvelope<T> of(String eventType, String aggregateId, long version, T payload) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .aggregateId(aggregateId)
                .version(version)
                .payload(payload)
                .build();
    }
}
