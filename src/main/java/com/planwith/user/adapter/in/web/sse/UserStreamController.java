package com.planwith.user.adapter.in.web.sse;

import com.planwith.user.adapter.in.gateway.AuthenticatedUserContext;
import com.planwith.user.adapter.in.gateway.GatewayAuthenticationContextResolver;
import com.planwith.user.application.port.in.IssueSseTicketUseCase;
import com.planwith.user.application.port.out.SseTicketPort;
import com.planwith.user.global.common.ApiResponse;
import com.planwith.user.global.config.AppProperties;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class UserStreamController {

    private final IssueSseTicketUseCase issueSseTicketUseCase;
    private final SseTicketPort sseTicketPort;
    private final AppProperties appProperties;
    private final ConcurrentHashMap<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    @PostMapping("/api/v1/sse/tickets")
    public ResponseEntity<ApiResponse<Map<String, String>>> issueTicket(AuthenticatedUserContext userContext) {
        AuthenticatedUserContext authenticated = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        String ticket = issueSseTicketUseCase.issueTicket(authenticated.userId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("ticket", ticket)));
    }

    @GetMapping(value = "/api/v1/stream/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam("ticket") String ticket) throws IOException {
        String userId = sseTicketPort.consume(ticket)
                .orElseThrow(() -> new CustomException(ErrorCode.SSE_TICKET_INVALID));

        long timeoutMs = appProperties.getSse().getEmitterTimeout().toMillis();
        SseEmitter emitter = new SseEmitter(timeoutMs);
        userEmitters.put(userId, emitter);
        emitter.onCompletion(() -> userEmitters.remove(userId, emitter));
        emitter.onTimeout(() -> {
            userEmitters.remove(userId, emitter);
            emitter.complete();
        });
        emitter.onError(ex -> userEmitters.remove(userId, emitter));

        SseEventPayload<Map<String, String>> connected =
                SseEventPayload.of(UUID.randomUUID().toString(), Map.of("userId", userId, "status", "connected"));
        emitter.send(SseEmitter.event().id(connected.eventId()).name("connected").data(connected));
        return emitter;
    }
}
