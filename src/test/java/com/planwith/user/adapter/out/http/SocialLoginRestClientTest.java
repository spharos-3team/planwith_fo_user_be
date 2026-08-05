package com.planwith.user.adapter.out.http;

import com.planwith.user.application.port.out.SocialUserInfoPort;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SocialLoginRestClientTest {

    private MockRestServiceServer server;
    private SocialLoginRestClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SocialLoginRestClient(
                builder,
                "https://example.test/google",
                "https://example.test/kakao",
                "https://example.test/naver"
        );
    }

    @Test
    @DisplayName("Google userinfo maps sub/email/name")
    void googleUserInfo() {
        server.expect(requestTo("https://example.test/google"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer tok-g"))
                .andRespond(withSuccess("""
                        {"sub":"g-1","email":"g@example.com","name":"G User"}
                        """, MediaType.APPLICATION_JSON));

        SocialUserInfoPort.SocialUserInfo info = client.getUserInfo(LoginType.GOOGLE, "tok-g");
        assertThat(info.getProviderId()).isEqualTo("g-1");
        assertThat(info.getEmail()).isEqualTo("g@example.com");
        assertThat(info.getNickname()).isEqualTo("G User");
        server.verify();
    }

    @Test
    @DisplayName("Kakao userinfo maps nested account/profile")
    void kakaoUserInfo() {
        server.expect(requestTo("https://example.test/kakao"))
                .andRespond(withSuccess("""
                        {"id":12345,"kakao_account":{"email":"k@example.com","profile":{"nickname":"카카오"}}}
                        """, MediaType.APPLICATION_JSON));

        SocialUserInfoPort.SocialUserInfo info = client.getUserInfo(LoginType.KAKAO, "tok-k");
        assertThat(info.getProviderId()).isEqualTo("12345");
        assertThat(info.getEmail()).isEqualTo("k@example.com");
        assertThat(info.getNickname()).isEqualTo("카카오");
        server.verify();
    }

    @Test
    @DisplayName("Naver userinfo without response body fails")
    void naverMissingResponse() {
        server.expect(requestTo("https://example.test/naver"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getUserInfo(LoginType.NAVER, "tok-n"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SOCIAL_LOGIN_FAILED);
        server.verify();
    }
}
