package com.planwith.user.application.service;

import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.in.RefreshSessionUseCase;
import com.planwith.user.application.port.out.RefreshTokenGeneratorPort;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.auth.RefreshTokenSession;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshSessionService implements RefreshSessionUseCase {

    private final RefreshTokenGeneratorPort refreshTokenGeneratorPort;
    private final RefreshTokenSessionPort refreshTokenSessionPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    @Transactional
    public TokenPair refreshSession(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String tokenHash = refreshTokenGeneratorPort.hash(refreshToken);
        Optional<RefreshTokenSession> sessionOptional = refreshTokenSessionPort.findByTokenHash(tokenHash);
        if (sessionOptional.isEmpty()) {
            Optional<String> usedFamilyId = refreshTokenSessionPort.findUsedFamilyId(tokenHash);
            if (usedFamilyId.isPresent()) {
                refreshTokenSessionPort.markFamilyCompromised(usedFamilyId.get());
                throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
            }
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenSession session = sessionOptional.get();
        if (session.isExpired() || refreshTokenSessionPort.isFamilyCompromised(session.getFamilyId())) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = parseUserId(session.getUserId());
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.isSuspended()) {
            refreshTokenSessionPort.deleteByUserId(session.getUserId());
            throw new CustomException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (!user.isActive()) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshTokenSessionPort.deleteByTokenHash(tokenHash);
        return tokenIssuanceService.rotateTokens(user, session.getFamilyId(), session.getSessionId());
    }

    private static Long parseUserId(String userId) {
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
