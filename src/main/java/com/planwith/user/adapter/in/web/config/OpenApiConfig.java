package com.planwith.user.adapter.in.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"local", "local-direct"})
public class OpenApiConfig {

    @Bean
    public OpenAPI planWithOpenApi(
            @Value("${springdoc.server-url:http://localhost:8000}") String serverUrl
    ) {
        final String bearer = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("PlanWith FO User BE")
                        .description("""
                                로컬 Swagger UI입니다. Try it out 기본 서버는 Gateway(`:8000`)입니다.
                                인증 API로 accessToken을 받은 뒤 Authorize에 Bearer로 넣으세요.
                                Refresh Cookie는 브라우저 교차 출처 제한으로 Swagger에서 완전하지 않을 수 있습니다.
                                """)
                        .version("v1"))
                .servers(List.of(
                        new Server().url(serverUrl).description("Gateway (권장)"),
                        new Server().url("http://localhost:8080").description("BE 직접 (Gateway trust 헤더 필요)")
                ))
                .components(new Components().addSecuritySchemes(bearer,
                        new SecurityScheme()
                                .name(bearer)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(bearer));
    }
}
