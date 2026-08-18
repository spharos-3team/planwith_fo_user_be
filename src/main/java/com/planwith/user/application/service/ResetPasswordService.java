package com.planwith.user.application.service;

import com.planwith.user.application.port.in.ResetPasswordUseCase;
import com.planwith.user.application.port.in.VerifyEmailCodeUseCase;
import com.planwith.user.application.port.out.PasswordEncoderPort;
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
public class ResetPasswordService implements ResetPasswordUseCase {

    private final VerifyEmailCodeUseCase verifyEmailCodeUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyEmailCodeUseCase.verifyCode(email, code);

        User user = userRepositoryPort.findActiveByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.changePassword(passwordEncoderPort.encode(newPassword));
        userRepositoryPort.save(user);
    }
}
