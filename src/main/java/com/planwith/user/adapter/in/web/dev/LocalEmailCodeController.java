package com.planwith.user.adapter.in.web.dev;

import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.global.common.ApiResponse;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Local/test-only helper for temp-fe smoke testing.
 * Not active in prod.
 */
@RestController
@RequestMapping("/api/v1/dev")
@Profile({"local", "local-direct", "test"})
@RequiredArgsConstructor
public class LocalEmailCodeController {

    private final EmailVerificationPort emailVerificationPort;

    @Value("${email.verification.mock-mode:false}")
    private boolean mockMode;

    @GetMapping("/email-code")
    public ResponseEntity<ApiResponse<String>> latestCode(@RequestParam String email) {
        if (!mockMode) {
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        }
        String code = emailVerificationPort.findLatestByEmail(email)
                .map(v -> v.getCode())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(code));
    }
}
