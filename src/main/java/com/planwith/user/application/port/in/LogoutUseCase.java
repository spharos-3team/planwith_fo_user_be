package com.planwith.user.application.port.in;

public interface LogoutUseCase {

    void logout(String refreshToken);

    void logoutAll(String userId);
}
