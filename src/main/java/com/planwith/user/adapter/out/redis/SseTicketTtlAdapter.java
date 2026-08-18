package com.planwith.user.adapter.out.redis;

import com.planwith.user.application.port.out.SseTicketTtlPort;
import com.planwith.user.global.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseTicketTtlAdapter implements SseTicketTtlPort {

    private final AppProperties appProperties;

    @Override
    public long ticketTtlSeconds() {
        return Math.max(appProperties.getSse().getTicketTtl().toSeconds(), 1L);
    }
}
