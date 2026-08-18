package com.planwith.user.adapter.out.http;

import com.planwith.user.domain.user.LoginType;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SocialOAuthTokenClientTest {

    private MockRestServiceServer server;
    private SocialOAuthTokenClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SocialOAuthTokenClient(
                builder,
                "https://example.test/google/token",
                "g-id",
                "g-secret",
                "https://example.test/kakao/token",
                "k-id",
                "k-secret",
                "https://example.test/naver/token",
                "n-id",
                "n-secret"
        );
    }

    @Test
    @DisplayName("exchanges Google authorization code for access token")
    void exchangeGoogle() {
        server.expect(requestTo("https://example.test/google/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess("{\"access_token\":\"g-access\",\"token_type\":\"Bearer\"}", MediaType.APPLICATION_JSON));

        String token = client.exchangeAuthorizationCode(
                LoginType.GOOGLE, "code-1", "https://app/callback", null);
        assertThat(token).isEqualTo("g-access");
        server.verify();
    }

    @Test
    @DisplayName("fails when client credentials are missing")
    void missingCredentials() {
        RestClient.Builder builder = RestClient.builder();
        SocialOAuthTokenClient bare = new SocialOAuthTokenClient(
                builder,
                "https://example.test/google/token",
                "",
                "",
                "https://example.test/kakao/token",
                "",
                "",
                "https://example.test/naver/token",
                "",
                ""
        );

        assertThatThrownBy(() -> bare.exchangeAuthorizationCode(
                LoginType.GOOGLE, "code", "https://app/cb", null))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SOCIAL_OAUTH_MISCONFIGURED);
    }
}
