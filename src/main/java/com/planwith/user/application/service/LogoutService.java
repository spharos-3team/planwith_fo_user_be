package com.planwith.user.application.service;

import com.planwith.user.application.port.in.LogoutUseCase;
import com.planwith.user.application.port.out.RefreshTokenGeneratorPort;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.domain.auth.RefreshTokenSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenGeneratorPort refreshTokenGeneratorPort;
    private final RefreshTokenSessionPort refreshTokenSessionPort;

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String tokenHash = refreshTokenGeneratorPort.hash(refreshToken);
        refreshTokenSessionPort.findByTokenHash(tokenHash)
                .map(RefreshTokenSession::getFamilyId)
                .ifPresent(refreshTokenSessionPort::deleteFamily);
    }

    @Override
    @Transactional
    public void logoutAll(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        refreshTokenSessionPort.deleteByUserId(userId);
    }
}
