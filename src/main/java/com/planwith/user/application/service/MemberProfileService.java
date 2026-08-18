package com.planwith.user.application.service;

import com.planwith.user.application.dto.MemberProfileInfo;
import com.planwith.user.application.port.in.GetMemberProfileUseCase;
import com.planwith.user.application.port.in.GetMyProfileUseCase;
import com.planwith.user.application.port.in.UpdateMyProfileUseCase;
import com.planwith.user.application.port.out.FollowPort;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService implements GetMyProfileUseCase, GetMemberProfileUseCase, UpdateMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final FollowPort followPort;
    private final ProfanityFilterPort profanityFilterPort;

    @Override
    public MemberProfileInfo getMyProfile(Long memberId) {
        User user = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.isSuspended()) {
            throw new CustomException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (!user.isActive()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return toInfo(user, true, null);
    }

    @Override
    public MemberProfileInfo getByMemberUuid(String memberUuid, Long viewerMemberIdOrNull) {
        User user = userRepositoryPort.findActiveByMemberUuid(memberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return toPublicInfo(user, viewerMemberIdOrNull);
    }

    @Override
    public MemberProfileInfo getByNickname(String nickname, Long viewerMemberIdOrNull) {
        if (!StringUtils.hasText(nickname)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        User user = userRepositoryPort.findActiveByNickname(nickname.trim())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return toPublicInfo(user, viewerMemberIdOrNull);
    }

    private MemberProfileInfo toPublicInfo(User user, Long viewerMemberIdOrNull) {
        Boolean followedByMe = null;
        if (viewerMemberIdOrNull != null) {
            User viewer = userRepositoryPort.findActiveById(viewerMemberIdOrNull).orElse(null);
            if (viewer != null && !viewer.getMemberUuid().equals(user.getMemberUuid())) {
                followedByMe = followPort.find(viewer.getMemberUuid(), user.getMemberUuid())
                        .map(FollowPort.FollowRelation::active)
                        .orElse(false);
            } else if (viewer != null) {
                followedByMe = false;
            }
        }
        return toInfo(user, false, followedByMe);
    }

    @Override
    @Transactional
    public MemberProfileInfo updateMyProfile(Long memberId, String nickname, String profileImage, String profileIntro) {
        User user = userRepositoryPort.findActiveById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (StringUtils.hasText(nickname)) {
            String trimmed = nickname.trim();
            if (trimmed.length() < 2 || trimmed.length() > 10) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR);
            }
            profanityFilterPort.validate(trimmed);
            if (!trimmed.equals(user.getNickname())
                    && userRepositoryPort.existsActiveByNicknameExcludingMemberId(trimmed, memberId)) {
                throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
            }
        }
        if (profileIntro != null) {
            if (profileIntro.length() > 20) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR);
            }
            if (StringUtils.hasText(profileIntro)) {
                profanityFilterPort.validate(profileIntro);
            }
        }

        user.updateProfile(nickname, profileImage, profileIntro);
        User saved = userRepositoryPort.save(user);
        return toInfo(saved, true, null);
    }

    private MemberProfileInfo toInfo(User user, boolean includeEmail, Boolean followedByMe) {
        return MemberProfileInfo.builder()
                .memberId(includeEmail ? user.getId() : null)
                .memberUuid(user.getMemberUuid())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .profileIntro(user.getIntroduction())
                .grade(user.getGrade())
                .email(includeEmail ? user.getEmail() : null)
                .followerCount(followPort.countFollowers(user.getMemberUuid()))
                .followingCount(followPort.countFollowing(user.getMemberUuid()))
                .followedByMe(followedByMe)
                .build();
    }
}
