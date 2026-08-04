package com.planwith.user.adapter.out.http;

import com.planwith.user.application.port.out.SocialUserInfoPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SocialLoginRestClient implements SocialUserInfoPort {

    private final RestClient restClient;
    private final String googleUserInfoUri;
    private final String kakaoUserInfoUri;
    private final String naverUserInfoUri;

    public SocialLoginRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${oauth.google.userinfo-uri}") String googleUserInfoUri,
            @Value("${oauth.kakao.userinfo-uri}") String kakaoUserInfoUri,
            @Value("${oauth.naver.userinfo-uri}") String naverUserInfoUri) {
        this.restClient = restClientBuilder.build();
        this.googleUserInfoUri = googleUserInfoUri;
        this.kakaoUserInfoUri = kakaoUserInfoUri;
        this.naverUserInfoUri = naverUserInfoUri;
    }

    @Override
    public SocialUserInfo getUserInfo(LoginType loginType, String accessToken) {
        return switch (loginType) {
            case GOOGLE -> getGoogleUserInfo(accessToken);
            case KAKAO -> getKakaoUserInfo(accessToken);
            case NAVER -> getNaverUserInfo(accessToken);
            default -> throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        };
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo getGoogleUserInfo(String accessToken) {
        Map<String, Object> body = requestUserInfo(googleUserInfoUri, accessToken);
        return SocialUserInfo.builder()
                .providerId(String.valueOf(body.get("sub")))
                .email((String) body.get("email"))
                .nickname((String) body.get("name"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo getKakaoUserInfo(String accessToken) {
        Map<String, Object> body = requestUserInfo(kakaoUserInfoUri, accessToken);
        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = (kakaoAccount != null)
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : null;

        return SocialUserInfo.builder()
                .providerId(String.valueOf(body.get("id")))
                .email(kakaoAccount != null ? (String) kakaoAccount.get("email") : null)
                .nickname(profile != null ? (String) profile.get("nickname") : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo getNaverUserInfo(String accessToken) {
        Map<String, Object> body = requestUserInfo(naverUserInfoUri, accessToken);
        Map<String, Object> response = (Map<String, Object>) body.get("response");

        return SocialUserInfo.builder()
                .providerId((String) response.get("id"))
                .email((String) response.get("email"))
                .nickname((String) response.get("nickname"))
                .build();
    }

    private Map<String, Object> requestUserInfo(String uri, String accessToken) {
        try {
            Map<String, Object> result = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (result == null) {
                throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
            }
            return result;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
