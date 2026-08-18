package com.planwith.user.application.port.out;

import com.planwith.user.domain.user.LoginType;
import lombok.Builder;
import lombok.Getter;

public interface SocialUserInfoPort {

    SocialUserInfo getUserInfo(LoginType loginType, String accessToken);

    @Getter
    @Builder
    class SocialUserInfo {
        private final String providerId;
        private final String email;
        private final String nickname;
    }
}
