package com.planwith.user.application.service;

import com.planwith.user.application.port.in.IssueSseTicketUseCase;
import com.planwith.user.application.port.out.SseTicketPort;
import com.planwith.user.application.port.out.SseTicketTtlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class IssueSseTicketService implements IssueSseTicketUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SseTicketPort sseTicketPort;
    private final SseTicketTtlPort sseTicketTtlPort;

    @Override
    public String issueTicket(String userId) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sseTicketPort.save(ticket, userId, sseTicketTtlPort.ticketTtlSeconds());
        return ticket;
    }
}
