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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialSignUpService implements SocialSignUpUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final SocialUserInfoPort socialUserInfoPort;
    private final SocialAccessTokenResolver socialAccessTokenResolver;
    private final ProfanityFilterPort profanityFilterPort;
    private final TermsPort termsPort;
    private final UserAgreementPort userAgreementPort;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    @Transactional
    public TokenPair socialSignUp(
            String provider,
            String accessToken,
            String authorizationCode,
            String redirectUri,
            String state,
            String nickname,
            List<Long> agreedTermIds
    ) {
        LoginType loginType = parseLoginType(provider);
        String resolvedToken = socialAccessTokenResolver.resolve(
                loginType, accessToken, authorizationCode, redirectUri, state);
        SocialUserInfoPort.SocialUserInfo socialUserInfo = socialUserInfoPort.getUserInfo(loginType, resolvedToken);

        boolean alreadyRegistered = userRepositoryPort
                .findActiveByLoginTypeAndProviderId(loginType, socialUserInfo.getProviderId())
                .isPresent();
        if (alreadyRegistered) {
            throw new CustomException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        validateRequiredAgreed(agreedTermIds);

        String rawNickname = StringUtils.hasText(nickname)
                ? nickname
                : socialUserInfo.getNickname();
        profanityFilterPort.validate(rawNickname);
        String uniqueNickname = generateUniqueNickname(rawNickname);

        String email = StringUtils.hasText(socialUserInfo.getEmail())
                ? socialUserInfo.getEmail()
                : loginType.name().toLowerCase() + "_" + socialUserInfo.getProviderId() + "@social.local";

        User user = User.createSocial(
                User.DEFAULT_GRADE,
                email,
                uniqueNickname,
                loginType,
                socialUserInfo.getProviderId()
        );

        User saved = userRepositoryPort.save(user);
        userAgreementPort.saveAgreements(saved.getMemberUuid(), agreedTermIds);

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
        String candidate = StringUtils.hasText(base) ? base : "사용자";
        if (candidate.length() > 10) {
            candidate = candidate.substring(0, 10);
        }
        String result = candidate;
        int suffix = 0;
        while (userRepositoryPort.existsActiveByNickname(result)) {
            suffix++;
            String suffixText = String.valueOf(suffix);
            int maxBase = Math.max(1, 10 - suffixText.length());
            result = candidate.substring(0, Math.min(candidate.length(), maxBase)) + suffixText;
            if (suffix > 9999) {
                result = UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }
        return result;
    }
}
