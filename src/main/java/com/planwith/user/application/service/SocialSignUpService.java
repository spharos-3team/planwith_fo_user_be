package com.planwith.user.application.service;

import com.planwith.user.application.dto.TokenPair;
import com.planwith.user.application.port.in.SocialSignUpUseCase;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.application.port.out.SocialUserInfoPort;
import com.planwith.user.application.port.out.TermsPort;
import com.planwith.user.application.port.out.UserAgreementPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
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
public class SocialSignUpService implements SocialSignUpUseCase {

    private static final Long DEFAULT_GRADE_ID = 1L;

    private final UserRepositoryPort userRepositoryPort;
    private final SocialUserInfoPort socialUserInfoPort;
    private final ProfanityFilterPort profanityFilterPort;
    private final TermsPort termsPort;
    private final UserAgreementPort userAgreementPort;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    @Transactional
    public TokenPair socialSignUp(String provider, String accessToken, String nickname, List<Long> agreedTermIds) {
        LoginType loginType = parseLoginType(provider);
        SocialUserInfoPort.SocialUserInfo socialUserInfo = socialUserInfoPort.getUserInfo(loginType, accessToken);

        boolean alreadyRegistered = userRepositoryPort
                .findActiveByLoginTypeAndProviderId(loginType, socialUserInfo.getProviderId())
                .isPresent();
        if (alreadyRegistered) {
            throw new CustomException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        validateRequiredAgreed(agreedTermIds);

        String rawNickname = (nickname != null && !nickname.isBlank())
                ? nickname
                : socialUserInfo.getNickname();
        profanityFilterPort.validate(rawNickname);
        String uniqueNickname = generateUniqueNickname(rawNickname);

        User user = User.createSocial(
                DEFAULT_GRADE_ID,
                socialUserInfo.getEmail(),
                uniqueNickname,
                loginType,
                socialUserInfo.getProviderId()
        );

        User saved = userRepositoryPort.save(user);
        userAgreementPort.saveAgreements(saved.getId(), agreedTermIds);

        return tokenIssuanceService.issueTokens(saved);
    }

    private LoginType parseLoginType(String provider) {
        try {
            return LoginType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    private void validateRequiredAgreed(List<Long> agreedTermIds) {
        List<Long> requiredIds = termsPort.findRequiredActiveIds();
        Set<Long> agreedSet = agreedTermIds == null ? Set.of() : Set.copyOf(agreedTermIds);
        boolean allRequiredAgreed = requiredIds.stream().allMatch(agreedSet::contains);
        if (!allRequiredAgreed) {
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private String generateUniqueNickname(String base) {
        String candidate = (base == null || base.isBlank()) ? "사용자" : base;
        if (candidate.length() > 10) {
            candidate = candidate.substring(0, 10);
        }
        String result = candidate;
        int suffix = 0;
        while (userRepositoryPort.existsActiveByNickname(result)) {
            suffix++;
            result = candidate + suffix;
        }
        return result;
    }
}
