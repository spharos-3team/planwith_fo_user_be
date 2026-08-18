package com.planwith.user.application.port.out;

import com.planwith.user.domain.auth.RefreshTokenSession;

import java.util.Optional;

public interface RefreshTokenSessionPort {

    void save(RefreshTokenSession session);

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    Optional<String> findUsedFamilyId(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void deleteByUserId(String userId);

    void deleteFamily(String familyId);

    boolean markFamilyCompromised(String familyId);

    boolean isFamilyCompromised(String familyId);
}
