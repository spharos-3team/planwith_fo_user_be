package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.TokenPair;

public interface RefreshSessionUseCase {

    TokenPair refreshSession(String refreshToken);
}
