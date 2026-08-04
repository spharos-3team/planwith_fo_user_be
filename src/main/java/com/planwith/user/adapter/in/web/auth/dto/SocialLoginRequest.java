package com.planwith.user.adapter.in.web.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

    @NotBlank(message = "provider가 필요합니다. (GOOGLE, KAKAO, NAVER)")
    private String provider;

    /** Provider access token from the client SDK (optional if authorizationCode is sent). */
    private String accessToken;

    /** Authorization code from OAuth redirect (optional if accessToken is sent). */
    private String authorizationCode;

    /** Must match the redirect_uri used at the authorize step when exchanging a code. */
    private String redirectUri;

    /** Required by Naver when state was used in the authorize step. */
    private String state;

    @AssertTrue(message = "accessToken 또는 authorizationCode(+redirectUri)가 필요합니다.")
    public boolean isCredentialPresent() {
        if (StringUtils.hasText(accessToken)) {
            return true;
        }
        return StringUtils.hasText(authorizationCode) && StringUtils.hasText(redirectUri);
    }
}
