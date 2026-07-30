package com.planwith.user.application.service;

import com.planwith.user.application.port.in.VerifyEmailCodeUseCase;
import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.domain.email.EmailVerification;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailCodeService implements VerifyEmailCodeUseCase {

    private final EmailVerificationPort emailVerificationPort;

    @Override
    @Transactional
    public void verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationPort.findLatestByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
        if (!verification.matchesCode(code)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_MISMATCH);
        }

        emailVerificationPort.markVerified(verification.getId());
    }
}
