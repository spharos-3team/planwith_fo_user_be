package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.TokenPair;

public interface LoginUseCase {
    TokenPair login(String email, String password);
}
