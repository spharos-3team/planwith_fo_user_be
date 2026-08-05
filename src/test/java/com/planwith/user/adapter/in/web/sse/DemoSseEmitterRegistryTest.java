package com.planwith.user.adapter.in.web.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class DemoSseEmitterRegistryTest {

    @Test
    @DisplayName("unregister removes emitter on cleanup")
    void unregister_removesEmitter() {
        DemoSseEmitterRegistry registry = new DemoSseEmitterRegistry();
        SseEmitter emitter = registry.register(1000L);
        assertThat(registry.size()).isEqualTo(1);
        registry.unregister(emitter);
        assertThat(registry.size()).isZero();
    }
}
