package com.planwith.user.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Placeholder for Testcontainers-backed integration tests.
 * Run with: {@code ./gradlew integrationTest} (Docker required).
 */
@EnabledIfEnvironmentVariable(named = "RUN_TESTCONTAINERS", matches = "true")
class PlaceholderIntegrationTest {

    @Test
    void placeholder() {
        // Add MySQL/Redis/Kafka/Mongo Testcontainers suites here.
    }
}
