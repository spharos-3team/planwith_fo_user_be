package com.planwith.user.adapter.out.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.planwith.user.application.port.out.AccessTokenPort;
import com.planwith.user.application.port.out.TokenExpirePropertiesPort;
import com.planwith.user.global.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class RsaAccessTokenAdapter implements AccessTokenPort, TokenExpirePropertiesPort {

    private final JwtEncoder jwtEncoder;
    private final RSAKey rsaJwk;
    private final AppProperties appProperties;

    public RsaAccessTokenAdapter(AppProperties appProperties, Environment environment, ResourceLoader resourceLoader) {
        this.appProperties = appProperties;
        AppProperties.Jwt jwt = appProperties.getJwt();
        LoadedKeys keys = loadKeys(jwt, environment, resourceLoader);
        this.rsaJwk = new RSAKey.Builder(keys.publicKey())
                .privateKey(keys.privateKey())
                .keyID(jwt.getKeyId())
                .build();
        this.jwtEncoder = new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(new JWKSet(rsaJwk)));
    }

    @Override
    public IssuedAccessToken issue(AccessTokenCommand command) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(appProperties.getJwt().getAccessTokenTtl());
        String scope = String.join(" ", command.scopes());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .subject(command.userId())
                .audience(List.of(appProperties.getJwt().getAudience()))
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("roles", command.roles())
                .claim("scope", scope)
                .claim("session_id", command.sessionId())
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(appProperties.getJwt().getKeyId())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(token, expiresAt, appProperties.getJwt().getAccessTokenTtl().toSeconds());
    }

    @Override
    public Map<String, Object> jwks() {
        try {
            return new JWKSet(rsaJwk.toPublicJWK()).toJSONObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build JWKS", e);
        }
    }

    @Override
    public long getAccessTokenExpireMs() {
        return appProperties.getJwt().getAccessTokenTtl().toMillis();
    }

    @Override
    public long getRefreshTokenExpireMs() {
        return appProperties.getJwt().getRefreshTokenTtl().toMillis();
    }

    private LoadedKeys loadKeys(AppProperties.Jwt jwt, Environment environment, ResourceLoader resourceLoader) {
        String privatePem = firstNonBlank(jwt.getPrivateKeyPem(), readPem(resourceLoader, jwt.getPrivateKeyPath()));
        String publicPem = firstNonBlank(jwt.getPublicKeyPem(), readPem(resourceLoader, jwt.getPublicKeyPath()));

        if (isBlank(privatePem) || isBlank(publicPem)) {
            if (!isLocalFriendlyProfile(environment)) {
                throw new IllegalStateException(
                        "JWT RSA keys must be configured via PEM content or file path outside local profiles");
            }
            log.warn("JWT PEM keys missing — generating ephemeral RSA 2048 keypair for local/test profile only");
            KeyPair keyPair = generateRsaKeyPair();
            return new LoadedKeys((RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        }
        return new LoadedKeys(parsePrivateKey(privatePem), parsePublicKey(publicPem));
    }

    private static boolean isLocalFriendlyProfile(Environment environment) {
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        if (profiles.contains("local") || profiles.contains("local-direct") || profiles.contains("test")) {
            return true;
        }
        return environment.getActiveProfiles().length == 0
                && "local".equals(environment.getProperty("spring.profiles.default", "local"));
    }

    private static String readPem(ResourceLoader resourceLoader, String path) {
        if (isBlank(path)) {
            return "";
        }
        Resource resource = resourceLoader.getResource(
                path.startsWith("classpath:") || path.startsWith("file:") ? path : "file:" + path);
        if (!resource.exists()) {
            throw new IllegalStateException("JWT key file not found: " + path);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JWT key file: " + path, e);
        }
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA keypair", e);
        }
    }

    private static RSAPrivateKey parsePrivateKey(String pem) {
        try {
            byte[] decoded = decodePem(pem, "PRIVATE KEY");
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT private key PEM", e);
        }
    }

    private static RSAPublicKey parsePublicKey(String pem) {
        try {
            byte[] decoded = decodePem(pem, "PUBLIC KEY");
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT public key PEM", e);
        }
    }

    private static byte[] decodePem(String pem, String type) {
        String normalized = pem
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private static String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        return second == null ? "" : second;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record LoadedKeys(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
    }
}
