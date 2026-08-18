package com.planwith.user.application.port.out;

import com.planwith.user.domain.email.EmailVerification;

import java.util.Optional;

public interface EmailVerificationPort {

    void saveNewCode(String email, String code);

    Optional<EmailVerification> findLatestByEmail(String email);

    void markVerified(Long id);

    boolean isEmailVerified(String email);
}
