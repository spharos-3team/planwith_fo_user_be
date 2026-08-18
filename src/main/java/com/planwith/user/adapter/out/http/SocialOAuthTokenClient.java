package com.planwith.user.adapter.out.http;

import com.planwith.user.application.port.out.SocialOAuthTokenPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class SocialOAuthTokenClient implements SocialOAuthTokenPort {

    private final RestClient restClient;
    private final ProviderConfig google;
    private final ProviderConfig kakao;
    private final ProviderConfig naver;

    public SocialOAuthTokenClient(
            RestClient.Builder restClientBuilder,
            @Value("${oauth.google.token-uri}") String googleTokenUri,
            @Value("${oauth.google.client-id:}") String googleClientId,
            @Value("${oauth.google.client-secret:}") String googleClientSecret,
            @Value("${oauth.kakao.token-uri}") String kakaoTokenUri,
            @Value("${oauth.kakao.client-id:}") String kakaoClientId,
            @Value("${oauth.kakao.client-secret:}") String kakaoClientSecret,
            @Value("${oauth.naver.token-uri}") String naverTokenUri,
            @Value("${oauth.naver.client-id:}") String naverClientId,
            @Value("${oauth.naver.client-secret:}") String naverClientSecret) {
        this.restClient = restClientBuilder.build();
        this.google = new ProviderConfig(googleTokenUri, googleClientId, googleClientSecret);
        this.kakao = new ProviderConfig(kakaoTokenUri, kakaoClientId, kakaoClientSecret);
        this.naver = new ProviderConfig(naverTokenUri, naverClientId, naverClientSecret);
    }

    @Override
    public String exchangeAuthorizationCode(
            LoginType loginType,
            String authorizationCode,
            String redirectUri,
            String state
    ) {
        return switch (loginType) {
            case GOOGLE -> exchange(google, authorizationCode, redirectUri, null);
            case KAKAO -> exchange(kakao, authorizationCode, redirectUri, null);
            case NAVER -> exchange(naver, authorizationCode, redirectUri, state);
            default -> throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        };
    }

    private String exchange(ProviderConfig config, String code, String redirectUri, String state) {
        if (!StringUtils.hasText(config.clientId()) || !StringUtils.hasText(config.clientSecret())) {
            throw new CustomException(ErrorCode.SOCIAL_OAUTH_MISCONFIGURED);
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", config.clientId());
        form.add("client_secret", config.clientSecret());
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        if (StringUtils.hasText(state)) {
            form.add("state", state);
        }

        try {
            Map<String, Object> body = restClient.post()
                    .uri(config.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (body == null || body.get("access_token") == null) {
                throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
            }
            return String.valueOf(body.get("access_token"));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Social token exchange failed for uri={}", config.tokenUri());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    private record ProviderConfig(String tokenUri, String clientId, String clientSecret) {
    }
}
