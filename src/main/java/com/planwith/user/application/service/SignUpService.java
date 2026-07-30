package com.planwith.user.application.service;

import com.planwith.user.application.port.in.SignUpUseCase;
import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.application.port.out.PasswordEncoderPort;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.application.port.out.TermsPort;
import com.planwith.user.application.port.out.UserAgreementPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpService implements SignUpUseCase {

    private static final Long DEFAULT_GRADE_ID = 1L;

    private final UserRepositoryPort userRepositoryPort;
    private final EmailVerificationPort emailVerificationPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final ProfanityFilterPort profanityFilterPort;
    private final TermsPort termsPort;
    private final UserAgreementPort userAgreementPort;

    @Override
    @Transactional
    public void signUp(String email, String password, String nickname, String profileImage,
                       String introduction, List<Long> agreedTermIds) {
        if (userRepositoryPort.existsActiveByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }
        if (userRepositoryPort.existsActiveByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
        if (!emailVerificationPort.isEmailVerified(email)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        profanityFilterPort.validate(nickname);
        profanityFilterPort.validate(introduction);
        validateRequiredAgreed(agreedTermIds);

        User user = User.createLocal(
                DEFAULT_GRADE_ID,
                email,
                passwordEncoderPort.encode(password),
                nickname,
                profileImage,
                introduction
        );

        User saved = userRepositoryPort.save(user);
        userAgreementPort.saveAgreements(saved.getId(), agreedTermIds);
    }

    private void validateRequiredAgreed(List<Long> agreedTermIds) {
        List<Long> requiredIds = termsPort.findRequiredActiveIds();
        Set<Long> agreedSet = agreedTermIds == null ? Set.of() : Set.copyOf(agreedTermIds);
        boolean allRequiredAgreed = requiredIds.stream().allMatch(agreedSet::contains);
        if (!allRequiredAgreed) {
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }
}
