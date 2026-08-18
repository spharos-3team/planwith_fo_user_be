package com.planwith.user.adapter.in.web.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SocialSignUpRequest {

    @NotBlank(message = "provider가 필요합니다. (GOOGLE, KAKAO, NAVER)")
    private String provider;

    private String accessToken;
    private String authorizationCode;
    private String redirectUri;
    private String state;
    private String nickname;
    private List<Long> agreedTermIds;

    @AssertTrue(message = "accessToken 또는 authorizationCode(+redirectUri)가 필요합니다.")
    public boolean isCredentialPresent() {
        if (StringUtils.hasText(accessToken)) {
            return true;
        }
        return StringUtils.hasText(authorizationCode) && StringUtils.hasText(redirectUri);
    }
}
