package com.planwith.user.adapter.in.web.jwks;

import com.planwith.user.application.port.out.AccessTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final AccessTokenPort accessTokenPort;

    @GetMapping(value = "/oauth2/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        return accessTokenPort.jwks();
    }
}
