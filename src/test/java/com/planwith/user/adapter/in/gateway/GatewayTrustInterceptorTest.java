package com.planwith.user.adapter.in.gateway;

import com.planwith.user.global.config.AppProperties;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayTrustInterceptorTest {

    private GatewayTrustInterceptor interceptor;
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getGateway().setInternalToken("secret-token");
        appProperties.getGateway().setTrustCheckEnabled(true);
        interceptor = new GatewayTrustInterceptor(appProperties);
    }

    @Test
    @DisplayName("allows JWKS without gateway token")
    void jwks_exempt() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/jwks");
        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("rejects missing gateway internal token")
    void missingToken_rejected() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.GATEWAY_TRUST_FAILED);
    }

    @Test
    @DisplayName("accepts valid gateway internal token")
    void validToken_accepted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request.addHeader(GatewayTrustInterceptor.HEADER_INTERNAL_TOKEN, "secret-token");
        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        assertThat(allowed).isTrue();
        assertThat(request.getAttribute(GatewayTrustInterceptor.ATTR_TRUSTED)).isEqualTo(Boolean.TRUE);
    }
}
