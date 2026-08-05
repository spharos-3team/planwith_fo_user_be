package com.planwith.user.adapter.in.web.auth;

import com.planwith.user.global.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieWriter {

    private final AppProperties appProperties;

    public void write(HttpServletResponse response, String rawRefreshToken, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(rawRefreshToken, maxAge).toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString());
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        String cookieName = appProperties.getRefreshCookie().getName();
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private ResponseCookie buildCookie(String value, Duration maxAge) {
        AppProperties.RefreshCookie props = appProperties.getRefreshCookie();
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(props.getName(), value)
                .httpOnly(true)
                .secure(props.isSecure())
                .path(props.getPath())
                .sameSite(props.getSameSite())
                .maxAge(maxAge);
        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }
        return builder.build();
    }
}
