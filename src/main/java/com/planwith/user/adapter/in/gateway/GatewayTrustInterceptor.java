package com.planwith.user.adapter.in.gateway;

import com.planwith.user.global.config.AppProperties;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayTrustInterceptor implements HandlerInterceptor {

    public static final String HEADER_INTERNAL_TOKEN = "X-Gateway-Internal-Token";
    public static final String ATTR_TRUSTED = "gateway.trusted";

    private static final List<String> EXEMPT_PATTERNS = List.of(
            "/oauth2/jwks",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            // 프로필 이미지 등 브라우저가 직접 로드하는 정적 파일
            "/files",
            "/files/**",
            // local API docs (Try it out should target Gateway; UI itself is BE-local)
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    );

    private final AppProperties appProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isExempt(request.getRequestURI())) {
            request.setAttribute(ATTR_TRUSTED, Boolean.FALSE);
            return true;
        }

        if (!appProperties.getGateway().isTrustCheckEnabled()) {
            request.setAttribute(ATTR_TRUSTED, Boolean.TRUE);
            return true;
        }

        String configured = appProperties.getGateway().getInternalToken();
        if (configured == null || configured.isBlank()) {
            throw new CustomException(ErrorCode.GATEWAY_TRUST_MISCONFIGURED);
        }

        String presented = request.getHeader(HEADER_INTERNAL_TOKEN);
        if (presented == null || !constantTimeEquals(configured, presented)) {
            throw new CustomException(ErrorCode.GATEWAY_TRUST_FAILED);
        }

        request.setAttribute(ATTR_TRUSTED, Boolean.TRUE);
        return true;
    }

    private boolean isExempt(String uri) {
        for (String pattern : EXEMPT_PATTERNS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        byte[] left = expected.getBytes(StandardCharsets.UTF_8);
        byte[] right = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }
}
