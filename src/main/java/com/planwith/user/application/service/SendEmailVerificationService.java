package com.planwith.user.application.service;

import com.planwith.user.application.port.in.SendEmailVerificationUseCase;
import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.application.port.out.MailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class SendEmailVerificationService implements SendEmailVerificationUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationPort emailVerificationPort;
    private final MailSenderPort mailSenderPort;

    /**
     * Persist code in its own adapter transaction, then send mail outside the DB transaction
     * so SMTP/network latency does not hold DB locks.
     */
    @Override
    public void sendVerificationCode(String email) {
        String code = generateCode();
        emailVerificationPort.saveNewCode(email, code);
        mailSenderPort.sendVerificationCode(email, code);
    }

    private String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
