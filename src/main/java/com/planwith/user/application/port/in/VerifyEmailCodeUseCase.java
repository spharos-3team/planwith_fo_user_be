package com.planwith.user.application.port.in;

public interface VerifyEmailCodeUseCase {
    void verifyCode(String email, String code);
}
