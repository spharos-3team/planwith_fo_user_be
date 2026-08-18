package com.planwith.user.adapter.in.web.sse;

import com.planwith.user.global.common.ApiResponse;
import com.planwith.user.global.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/stream/demo")
@RequiredArgsConstructor
public class DemoStreamController {

    private final DemoSseEmitterRegistry demoSseEmitterRegistry;
    private final AppProperties appProperties;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "sse-demo-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() throws IOException {
        long timeoutMs = appProperties.getSse().getEmitterTimeout().toMillis();
        SseEmitter emitter = demoSseEmitterRegistry.register(timeoutMs);

        SseEventPayload<Map<String, String>> connected =
                SseEventPayload.of(UUID.randomUUID().toString(), Map.of("status", "connected"));
        emitter.send(SseEmitter.event().id(connected.eventId()).name("connected").data(connected));

        long heartbeatMs = appProperties.getSse().getHeartbeatInterval().toMillis();
        ScheduledFuture<?> heartbeat = heartbeatScheduler.scheduleAtFixedRate(
                () -> sendHeartbeat(emitter), heartbeatMs, heartbeatMs, TimeUnit.MILLISECONDS);

        Runnable cleanup = () -> {
            heartbeat.cancel(true);
            demoSseEmitterRegistry.unregister(emitter);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            cleanup.run();
            emitter.complete();
        });
        emitter.onError(ex -> cleanup.run());
        return emitter;
    }

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> publish(
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Object payload = body == null ? Map.of("message", "demo") : body;
        int delivered = demoSseEmitterRegistry.publish(payload);
        return ResponseEntity.ok(ApiResponse.success(Map.of("delivered", delivered)));
    }

    private void sendHeartbeat(SseEmitter emitter) {
        try {
            SseEventPayload<Map<String, String>> heartbeat =
                    SseEventPayload.of(UUID.randomUUID().toString(), Map.of("type", "heartbeat"));
            emitter.send(SseEmitter.event().id(heartbeat.eventId()).name("heartbeat").data(heartbeat));
        } catch (Exception e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
                // already completed
            }
        }
    }
}
