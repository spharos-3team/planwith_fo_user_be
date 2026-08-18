package com.planwith.user.adapter.out.security;

import com.planwith.user.application.port.out.AccessTokenPort;
import com.planwith.user.global.config.AppProperties;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RsaAccessTokenAdapterTest {

    private RsaAccessTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setIssuer("http://localhost:8080");
        properties.getJwt().setAudience("planwith-api");
        properties.getJwt().setKeyId("test-key");
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(14));

        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles("test");
        adapter = new RsaAccessTokenAdapter(properties, environment, new DefaultResourceLoader());
    }

    @Test
    @DisplayName("issued JWT uses RS256, kid, and required claims")
    void issue_containsRequiredClaimsAndKid() throws Exception {
        AccessTokenPort.IssuedAccessToken issued = adapter.issue(new AccessTokenPort.AccessTokenCommand(
                "42", List.of("USER"), List.of("profile:read", "plan:read"), "session-1"));

        SignedJWT jwt = SignedJWT.parse(issued.token());
        assertThat(jwt.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
        assertThat(jwt.getHeader().getKeyID()).isEqualTo("test-key");
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo("http://localhost:8080");
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("42");
        assertThat(jwt.getJWTClaimsSet().getAudience()).containsExactly("planwith-api");
        assertThat(jwt.getJWTClaimsSet().getJWTID()).isNotBlank();
        assertThat(jwt.getJWTClaimsSet().getIssueTime()).isNotNull();
        assertThat(jwt.getJWTClaimsSet().getNotBeforeTime()).isNotNull();
        assertThat(jwt.getJWTClaimsSet().getExpirationTime()).isNotNull();
        assertThat(jwt.getJWTClaimsSet().getClaim("roles")).isEqualTo(List.of("USER"));
        assertThat(jwt.getJWTClaimsSet().getStringClaim("scope")).isEqualTo("profile:read plan:read");
        assertThat(jwt.getJWTClaimsSet().getStringClaim("session_id")).isEqualTo("session-1");
        assertThat(issued.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    @DisplayName("JWKS exposes public key only")
    void jwks_exposesPublicKeyOnly() {
        Map<String, Object> jwks = adapter.jwks();
        String serialized = jwks.toString();
        assertThat(serialized).contains("keys");
        assertThat(serialized).doesNotContain("PRIVATE");
        assertThat(serialized.toLowerCase()).doesNotContain("private");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        assertThat(keys).isNotEmpty();
        assertThat(keys.getFirst()).containsKeys("kty", "kid", "n", "e");
        assertThat(keys.getFirst()).doesNotContainKey("d");
    }
}
