package com.planwith.user.adapter.in.web.config;

import com.planwith.user.adapter.in.gateway.GatewayAuthenticationContextResolver;
import com.planwith.user.adapter.in.gateway.GatewayTrustInterceptor;
import com.planwith.user.global.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;
    private final GatewayTrustInterceptor gatewayTrustInterceptor;
    private final GatewayAuthenticationContextResolver gatewayAuthenticationContextResolver;
    private final String uploadDir;

    public WebMvcConfig(
            AppProperties appProperties,
            GatewayTrustInterceptor gatewayTrustInterceptor,
            GatewayAuthenticationContextResolver gatewayAuthenticationContextResolver,
            @Value("${file.upload-dir:/tmp/planwith-uploads}") String uploadDir
    ) {
        this.appProperties = appProperties;
        this.gatewayTrustInterceptor = gatewayTrustInterceptor;
        this.gatewayAuthenticationContextResolver = gatewayAuthenticationContextResolver;
        this.uploadDir = uploadDir;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayTrustInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(gatewayAuthenticationContextResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        AppProperties.Cors cors = appProperties.getCors();
        if (!cors.isEnabled()) {
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(cors.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(cors.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(cors.getAllowedHeaders().toArray(String[]::new))
                .allowCredentials(cors.isAllowCredentials());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + location);
    }
}
