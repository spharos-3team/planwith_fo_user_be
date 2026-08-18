package com.planwith.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserApplicationTest {

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("application context loads with test profile and Eureka disabled")
    void contextLoads() {
        assertThat(environment.getActiveProfiles()).contains("test");
        assertThat(environment.getProperty("eureka.client.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("eureka.client.register-with-eureka")).isEqualTo("false");
    }
}
