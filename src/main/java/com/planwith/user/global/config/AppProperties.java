package com.planwith.user.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Gateway gateway = new Gateway();
    private final Jwt jwt = new Jwt();
    private final RefreshCookie refreshCookie = new RefreshCookie();
    private final Cors cors = new Cors();
    private final Sse sse = new Sse();

    @Getter
    @Setter
    public static class Gateway {
        private String internalToken = "";
        private boolean trustCheckEnabled = true;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String issuer = "http://localhost:8080";
        private String audience = "planwith-api";
        private String keyId = "fo-user-be-key-1";
        private String privateKeyPath = "";
        private String publicKeyPath = "";
        private String privateKeyPem = "";
        private String publicKeyPem = "";
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(14);
    }

    @Getter
    @Setter
    public static class RefreshCookie {
        private String name = "refresh_token";
        private boolean secure = false;
        private String sameSite = "Lax";
        private String domain = "";
        private String path = "/api/v1/auth";
    }

    @Getter
    @Setter
    public static class Cors {
        private boolean enabled = false;
        private List<String> allowedOrigins = new ArrayList<>();
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private boolean allowCredentials = true;
    }

    @Getter
    @Setter
    public static class Sse {
        private Duration ticketTtl = Duration.ofSeconds(30);
        private Duration emitterTimeout = Duration.ofMinutes(30);
        private Duration heartbeatInterval = Duration.ofSeconds(15);
    }
}
