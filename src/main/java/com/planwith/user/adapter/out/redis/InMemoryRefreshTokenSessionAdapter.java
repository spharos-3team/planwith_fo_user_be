package com.planwith.user.adapter.out.redis;

import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.domain.auth.RefreshTokenSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnMissingBean(StringRedisTemplate.class)
public class InMemoryRefreshTokenSessionAdapter implements RefreshTokenSessionPort {

    private final Map<String, RefreshTokenSession> byHash = new ConcurrentHashMap<>();
    private final Map<String, String> usedHashToFamily = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> familyToHashes = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userToHashes = new ConcurrentHashMap<>();
    private final Set<String> compromisedFamilies = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized void save(RefreshTokenSession session) {
        byHash.put(session.getTokenHash(), session);
        familyToHashes.computeIfAbsent(session.getFamilyId(), k -> ConcurrentHashMap.newKeySet())
                .add(session.getTokenHash());
        userToHashes.computeIfAbsent(session.getUserId(), k -> ConcurrentHashMap.newKeySet())
                .add(session.getTokenHash());
    }

    @Override
    public synchronized Optional<RefreshTokenSession> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(byHash.get(tokenHash));
    }

    @Override
    public synchronized Optional<String> findUsedFamilyId(String tokenHash) {
        return Optional.ofNullable(usedHashToFamily.get(tokenHash));
    }

    @Override
    public synchronized void deleteByTokenHash(String tokenHash) {
        RefreshTokenSession session = byHash.remove(tokenHash);
        if (session == null) {
            return;
        }
        usedHashToFamily.put(tokenHash, session.getFamilyId());
        Set<String> family = familyToHashes.get(session.getFamilyId());
        if (family != null) {
            family.remove(tokenHash);
        }
        Set<String> user = userToHashes.get(session.getUserId());
        if (user != null) {
            user.remove(tokenHash);
        }
    }

    @Override
    public synchronized void deleteByUserId(String userId) {
        Set<String> hashes = userToHashes.remove(userId);
        if (hashes == null) {
            return;
        }
        Set<String> families = ConcurrentHashMap.newKeySet();
        for (String hash : Set.copyOf(hashes)) {
            RefreshTokenSession session = byHash.get(hash);
            if (session != null) {
                families.add(session.getFamilyId());
            }
        }
        for (String familyId : families) {
            clearFamilyTokens(familyId);
        }
    }

    @Override
    public synchronized void deleteFamily(String familyId) {
        clearFamilyTokens(familyId);
    }

    @Override
    public synchronized boolean markFamilyCompromised(String familyId) {
        clearFamilyTokens(familyId);
        compromisedFamilies.add(familyId);
        return true;
    }

    @Override
    public synchronized boolean isFamilyCompromised(String familyId) {
        return compromisedFamilies.contains(familyId);
    }

    private void clearFamilyTokens(String familyId) {
        Set<String> hashes = familyToHashes.remove(familyId);
        if (hashes == null) {
            return;
        }
        for (String hash : hashes) {
            RefreshTokenSession session = byHash.remove(hash);
            usedHashToFamily.remove(hash);
            if (session != null) {
                Set<String> user = userToHashes.get(session.getUserId());
                if (user != null) {
                    user.remove(hash);
                }
            }
        }
    }
}
