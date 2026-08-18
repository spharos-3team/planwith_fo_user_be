package com.planwith.user.application.service;

import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.in.LoginUseCase;
import com.planwith.user.application.port.out.PasswordEncoderPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    @Transactional
    public TokenPair login(String email, String password) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (user.isSuspended()) {
            throw new CustomException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (!user.isActive()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getLoginType() != LoginType.LOCAL || user.getPassword() == null) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!passwordEncoderPort.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.recordLastLogin(LocalDateTime.now());
        User saved = userRepositoryPort.save(user);
        return tokenIssuanceService.issueTokens(saved);
    }
}
