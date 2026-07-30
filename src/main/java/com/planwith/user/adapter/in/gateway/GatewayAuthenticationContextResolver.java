package com.planwith.user.adapter.in.gateway;

import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;

@Component
public class GatewayAuthenticationContextResolver implements HandlerMethodArgumentResolver {

    public static final String HEADER_USER_ID = "X-Auth-User-Id";
    public static final String HEADER_ROLES = "X-Auth-Roles";
    public static final String HEADER_SCOPES = "X-Auth-Scopes";
    public static final String HEADER_SESSION_ID = "X-Auth-Session-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthenticatedUserContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return AuthenticatedUserContext.anonymous(null);
        }

        String requestId = request.getHeader(HEADER_REQUEST_ID);
        boolean trusted = Boolean.TRUE.equals(request.getAttribute(GatewayTrustInterceptor.ATTR_TRUSTED));
        if (!trusted) {
            return AuthenticatedUserContext.anonymous(requestId);
        }

        String userId = trimToNull(request.getHeader(HEADER_USER_ID));
        List<String> roles = splitCsv(request.getHeader(HEADER_ROLES));
        List<String> scopes = splitCsv(request.getHeader(HEADER_SCOPES));
        String sessionId = trimToNull(request.getHeader(HEADER_SESSION_ID));
        return new AuthenticatedUserContext(userId, roles, scopes, sessionId, requestId);
    }

    public static AuthenticatedUserContext requireAuthenticated(AuthenticatedUserContext context) {
        if (context == null || !context.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        }
        return context;
    }

    private static List<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
