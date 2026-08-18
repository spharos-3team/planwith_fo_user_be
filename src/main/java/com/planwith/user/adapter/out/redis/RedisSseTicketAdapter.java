package com.planwith.user.adapter.out.redis;

import com.planwith.user.application.port.out.SseTicketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Primary
@ConditionalOnBean(StringRedisTemplate.class)
@RequiredArgsConstructor
public class RedisSseTicketAdapter implements SseTicketPort {

    static final String KEY_PREFIX = "sse:ticket:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String ticket, String userId, long ttlSeconds) {
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, userId, Duration.ofSeconds(Math.max(ttlSeconds, 1)));
    }

    @Override
    public Optional<String> consume(String ticket) {
        String key = KEY_PREFIX + ticket;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId == null) {
            return Optional.empty();
        }
        redisTemplate.delete(key);
        return Optional.of(userId);
    }
}
