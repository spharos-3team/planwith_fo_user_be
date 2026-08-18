package com.planwith.user.application.service;

import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.domain.email.EmailVerification;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerifyEmailCodeServiceTest {

    @Mock
    private EmailVerificationPort emailVerificationPort;

    @InjectMocks
    private VerifyEmailCodeService verifyEmailCodeService;

    @Test
    @DisplayName("verifyCode marks latest matching code as verified")
    void verify_success() {
        EmailVerification pending = EmailVerification.builder()
                .id(7L)
                .email("a@b.com")
                .code("123456")
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();
        given(emailVerificationPort.findLatestByEmail("a@b.com")).willReturn(Optional.of(pending));

        verifyEmailCodeService.verifyCode("a@b.com", "123456");

        verify(emailVerificationPort).markVerified(7L);
    }

    @Test
    @DisplayName("verifyCode rejects expired code")
    void verify_expired() {
        EmailVerification expired = EmailVerification.builder()
                .id(7L)
                .email("a@b.com")
                .code("123456")
                .verified(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
        given(emailVerificationPort.findLatestByEmail("a@b.com")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> verifyEmailCodeService.verifyCode("a@b.com", "123456"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_EXPIRED);

        verify(emailVerificationPort, never()).markVerified(7L);
    }

    @Test
    @DisplayName("verifyCode rejects mismatched code")
    void verify_mismatch() {
        EmailVerification pending = EmailVerification.builder()
                .id(7L)
                .email("a@b.com")
                .code("123456")
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();
        given(emailVerificationPort.findLatestByEmail("a@b.com")).willReturn(Optional.of(pending));

        assertThatThrownBy(() -> verifyEmailCodeService.verifyCode("a@b.com", "000000"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_MISMATCH);

        verify(emailVerificationPort, never()).markVerified(7L);
    }
}
