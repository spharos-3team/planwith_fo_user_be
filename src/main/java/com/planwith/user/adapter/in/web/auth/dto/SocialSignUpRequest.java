package com.planwith.user.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SocialSignUpRequest {

    @NotBlank(message = "provider가 필요합니다. (GOOGLE, KAKAO, NAVER)")
    private String provider;

    @NotBlank(message = "소셜 액세스 토큰이 필요합니다.")
    private String accessToken;

    private String nickname;

    private List<Long> agreedTermIds;
}
