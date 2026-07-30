package com.planwith.user.application.service;

import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.application.port.out.PasswordEncoderPort;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.application.port.out.TermsPort;
import com.planwith.user.application.port.out.UserAgreementPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private EmailVerificationPort emailVerificationPort;
    @Mock
    private PasswordEncoderPort passwordEncoderPort;
    @Mock
    private ProfanityFilterPort profanityFilterPort;
    @Mock
    private TermsPort termsPort;
    @Mock
    private UserAgreementPort userAgreementPort;

    @InjectMocks
    private SignUpService signUpService;

    @Test
    @DisplayName("signUp saves local user when validations pass")
    void signUp_success() {
        given(userRepositoryPort.existsActiveByEmail("a@b.com")).willReturn(false);
        given(userRepositoryPort.existsActiveByNickname("nick")).willReturn(false);
        given(emailVerificationPort.isEmailVerified("a@b.com")).willReturn(true);
        given(termsPort.findRequiredActiveIds()).willReturn(List.of(1L, 2L, 3L));
        given(passwordEncoderPort.encode("Passw0rd!")).willReturn("encoded");
        given(userRepositoryPort.save(any(User.class))).willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return User.builder()
                    .id(99L)
                    .gradeId(u.getGradeId())
                    .email(u.getEmail())
                    .password(u.getPassword())
                    .nickname(u.getNickname())
                    .loginType(u.getLoginType())
                    .status(u.getStatus())
                    .role(u.getRole())
                    .build();
        });

        signUpService.signUp("a@b.com", "Passw0rd!", "nick", null, "hi", List.of(1L, 2L, 3L));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("a@b.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        verify(userAgreementPort).saveAgreements(99L, List.of(1L, 2L, 3L));
    }

    @Test
    @DisplayName("signUp rejects unverified email")
    void signUp_emailNotVerified() {
        given(userRepositoryPort.existsActiveByEmail("a@b.com")).willReturn(false);
        given(userRepositoryPort.existsActiveByNickname("nick")).willReturn(false);
        given(emailVerificationPort.isEmailVerified("a@b.com")).willReturn(false);

        assertThatThrownBy(() ->
                signUpService.signUp("a@b.com", "Passw0rd!", "nick", null, null, List.of(1L, 2L, 3L)))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);

        verify(userRepositoryPort, never()).save(any());
        verify(profanityFilterPort, never()).validate(anyString());
    }
}
