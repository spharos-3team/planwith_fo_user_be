package com.planwith.user.adapter.out.redis;

import com.planwith.user.application.port.out.SseTicketPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnMissingBean(StringRedisTemplate.class)
public class InMemorySseTicketAdapter implements SseTicketPort {

    private final Map<String, Entry> tickets = new ConcurrentHashMap<>();

    @Override
    public void save(String ticket, String userId, long ttlSeconds) {
        tickets.put(ticket, new Entry(userId, Instant.now().plusSeconds(Math.max(ttlSeconds, 1))));
    }

    @Override
    public Optional<String> consume(String ticket) {
        Entry entry = tickets.remove(ticket);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            return Optional.empty();
        }
        return Optional.of(entry.userId());
    }

    private record Entry(String userId, Instant expiresAt) {
    }
}
