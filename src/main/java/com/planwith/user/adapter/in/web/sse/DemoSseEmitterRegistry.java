package com.planwith.user.adapter.in.web.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DemoSseEmitterRegistry {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register(long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        emitters.add(emitter);
        return emitter;
    }

    public void unregister(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public int publish(Object payload) {
        SseEventPayload<Object> event = SseEventPayload.of(UUID.randomUUID().toString(), payload);
        int delivered = 0;
        for (SseEmitter emitter : List.copyOf(emitters)) {
            try {
                emitter.send(SseEmitter.event()
                        .id(event.eventId())
                        .name("demo")
                        .data(event));
                delivered++;
            } catch (IOException | IllegalStateException e) {
                unregister(emitter);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // emitter already closed
                }
            }
        }
        return delivered;
    }

    public int size() {
        return emitters.size();
    }
}
