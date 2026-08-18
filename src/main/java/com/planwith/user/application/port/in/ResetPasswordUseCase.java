package com.planwith.user.application.port.in;

public interface ResetPasswordUseCase {
    void resetPassword(String email, String code, String newPassword);
}
