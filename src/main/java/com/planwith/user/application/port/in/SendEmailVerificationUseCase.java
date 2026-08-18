package com.planwith.user.application.port.in;

public interface SendEmailVerificationUseCase {
    void sendVerificationCode(String email);
}
