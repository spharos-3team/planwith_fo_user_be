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
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedCode = code == null ? "" : code.trim().replaceAll("\\s+", "");

        EmailVerification verification = emailVerificationPort.findLatestByEmail(normalizedEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        // 이미 같은 코드로 인증된 최신 건이면 성공으로 처리 (재클릭/연타)
        if (verification.isVerified() && verification.matchesCode(normalizedCode) && !verification.isExpired()) {
            return;
        }

        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
        if (!verification.matchesCode(normalizedCode)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_MISMATCH);
        }

        emailVerificationPort.markVerified(verification.getId());
    }
}
