package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.TokenPair;

import java.util.List;

public interface SocialSignUpUseCase {
    TokenPair socialSignUp(String provider, String accessToken, String nickname, List<Long> agreedTermIds);
}
