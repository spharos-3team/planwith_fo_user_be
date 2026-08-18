package com.planwith.user.application.service;

import com.planwith.user.application.port.in.WithdrawUseCase;
import com.planwith.user.application.port.out.PasswordEncoderPort;
import com.planwith.user.application.port.out.RefreshTokenSessionPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawService implements WithdrawUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final RefreshTokenSessionPort refreshTokenSessionPort;

    @Override
    @Transactional
    public void withdraw(Long userId, String password) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.ALREADY_WITHDRAWN);
        }

        if (user.isLocalAccount()) {
            if (password == null || password.isBlank()
                    || user.getPassword() == null
                    || !passwordEncoderPort.matches(password, user.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
            }
        }

        // Soft delete only: status -> DELETED, row kept with anonymized PII.
        user.withdraw();
        userRepositoryPort.save(user);
        refreshTokenSessionPort.deleteByUserId(String.valueOf(userId));
    }
}
