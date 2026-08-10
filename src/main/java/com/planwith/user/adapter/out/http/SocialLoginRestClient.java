package com.planwith.user.adapter.out.http;

import com.planwith.user.application.port.out.SocialUserInfoPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
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
        if (!StringUtils.hasText(accessToken)) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
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
        String providerId = stringValue(body.get("sub"));
        requireProviderId(providerId);
        return SocialUserInfo.builder()
                .providerId(providerId)
                .email(stringValue(body.get("email")))
                .nickname(firstNonBlank(stringValue(body.get("name")), stringValue(body.get("given_name"))))
                .build();
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo getKakaoUserInfo(String accessToken) {
        Map<String, Object> body = requestUserInfo(kakaoUserInfoUri, accessToken);
        String providerId = stringValue(body.get("id"));
        requireProviderId(providerId);

        Map<String, Object> kakaoAccount = asMap(body.get("kakao_account"));
        Map<String, Object> profile = kakaoAccount != null ? asMap(kakaoAccount.get("profile")) : null;

        return SocialUserInfo.builder()
                .providerId(providerId)
                .email(kakaoAccount != null ? stringValue(kakaoAccount.get("email")) : null)
                .nickname(profile != null ? stringValue(profile.get("nickname")) : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo getNaverUserInfo(String accessToken) {
        Map<String, Object> body = requestUserInfo(naverUserInfoUri, accessToken);
        Map<String, Object> response = asMap(body.get("response"));
        if (response == null) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
        String providerId = stringValue(response.get("id"));
        requireProviderId(providerId);
        return SocialUserInfo.builder()
                .providerId(providerId)
                .email(stringValue(response.get("email")))
                .nickname(firstNonBlank(stringValue(response.get("nickname")), stringValue(response.get("name"))))
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
            if (result == null || result.isEmpty()) {
                throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
            }
            return result;
        } catch (CustomException e) {
            throw e;
        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.warn("Social userinfo request failed for uri={} status={} body={}",
                    uri, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        } catch (Exception e) {
            log.warn("Social userinfo request failed for uri={} cause={}", uri, e.toString());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    private static void requireProviderId(String providerId) {
        if (!StringUtils.hasText(providerId) || "null".equalsIgnoreCase(providerId)) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() || "null".equalsIgnoreCase(text) ? null : text;
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return StringUtils.hasText(second) ? second : null;
    }
}
