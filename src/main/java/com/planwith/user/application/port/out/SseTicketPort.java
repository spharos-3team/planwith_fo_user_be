package com.planwith.user.application.port.out;

import java.util.Optional;

public interface SseTicketPort {

    void save(String ticket, String userId, long ttlSeconds);

    Optional<String> consume(String ticket);
}
