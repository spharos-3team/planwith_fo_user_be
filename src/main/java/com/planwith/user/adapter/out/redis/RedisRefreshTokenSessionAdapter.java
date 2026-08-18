package com.planwith.user.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.domain.auth.RefreshTokenSession;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Component
@Primary
@ConditionalOnBean(StringRedisTemplate.class)
@RequiredArgsConstructor
public class RedisRefreshTokenSessionAdapter implements RefreshTokenSessionPort {

    static final String KEY_REFRESH = "auth:refresh:";
    static final String KEY_USED = "auth:refresh:used:";
    static final String KEY_FAMILY = "auth:family:";
    static final String KEY_FAMILY_REVOKED = "auth:family:revoked:";
    static final String KEY_USER_SESSIONS = "auth:user:sessions:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(RefreshTokenSession session) {
        Duration ttl = Duration.between(Instant.now(), session.getExpiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_REFRESH + session.getTokenHash(), toJson(session), ttl);
        redisTemplate.opsForSet().add(KEY_FAMILY + session.getFamilyId(), session.getTokenHash());
        redisTemplate.expire(KEY_FAMILY + session.getFamilyId(), ttl);
        redisTemplate.opsForSet().add(KEY_USER_SESSIONS + session.getUserId(), session.getTokenHash());
        redisTemplate.expire(KEY_USER_SESSIONS + session.getUserId(), ttl);
    }

    @Override
    public Optional<RefreshTokenSession> findByTokenHash(String tokenHash) {
        String payload = redisTemplate.opsForValue().get(KEY_REFRESH + tokenHash);
        return payload == null ? Optional.empty() : Optional.of(fromJson(payload));
    }

    @Override
    public Optional<String> findUsedFamilyId(String tokenHash) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_USED + tokenHash));
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        findByTokenHash(tokenHash).ifPresent(session -> {
            Duration ttl = Duration.between(Instant.now(), session.getExpiresAt());
            if (ttl.isNegative() || ttl.isZero()) {
                ttl = Duration.ofDays(14);
            }
            redisTemplate.delete(KEY_REFRESH + tokenHash);
            redisTemplate.opsForValue().set(KEY_USED + tokenHash, session.getFamilyId(), ttl);
            redisTemplate.opsForSet().remove(KEY_FAMILY + session.getFamilyId(), tokenHash);
            redisTemplate.opsForSet().remove(KEY_USER_SESSIONS + session.getUserId(), tokenHash);
        });
    }

    @Override
    public void deleteByUserId(String userId) {
        Set<String> hashes = redisTemplate.opsForSet().members(KEY_USER_SESSIONS + userId);
        if (hashes != null) {
            for (String hash : Set.copyOf(hashes)) {
                findByTokenHash(hash).ifPresent(session -> deleteFamily(session.getFamilyId()));
            }
        }
        redisTemplate.delete(KEY_USER_SESSIONS + userId);
    }

    @Override
    public void deleteFamily(String familyId) {
        Set<String> hashes = redisTemplate.opsForSet().members(KEY_FAMILY + familyId);
        if (hashes != null) {
            for (String hash : hashes) {
                Optional<RefreshTokenSession> session = findByTokenHash(hash);
                redisTemplate.delete(KEY_REFRESH + hash);
                redisTemplate.delete(KEY_USED + hash);
                session.ifPresent(s -> redisTemplate.opsForSet().remove(KEY_USER_SESSIONS + s.getUserId(), hash));
            }
        }
        redisTemplate.delete(KEY_FAMILY + familyId);
    }

    @Override
    public boolean markFamilyCompromised(String familyId) {
        redisTemplate.opsForValue().set(KEY_FAMILY_REVOKED + familyId, "1", Duration.ofDays(14));
        deleteFamily(familyId);
        return true;
    }

    @Override
    public boolean isFamilyCompromised(String familyId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_FAMILY_REVOKED + familyId));
    }

    private String toJson(RefreshTokenSession session) {
        try {
            return objectMapper.writeValueAsString(StoredSession.from(session));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize refresh session", e);
        }
    }

    private RefreshTokenSession fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, StoredSession.class).toDomain();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize refresh session", e);
        }
    }

    private record StoredSession(
            String userId, String tokenHash, String familyId, String sessionId, Instant expiresAt
    ) {
        static StoredSession from(RefreshTokenSession session) {
            return new StoredSession(session.getUserId(), session.getTokenHash(), session.getFamilyId(),
                    session.getSessionId(), session.getExpiresAt());
        }

        RefreshTokenSession toDomain() {
            return RefreshTokenSession.builder()
                    .userId(userId).tokenHash(tokenHash).familyId(familyId)
                    .sessionId(sessionId).expiresAt(expiresAt).build();
        }
    }
}
