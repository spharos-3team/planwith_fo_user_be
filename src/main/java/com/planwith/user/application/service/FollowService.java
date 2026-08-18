package com.planwith.user.application.service;

import com.planwith.user.application.dto.MemberProfileInfo;
import com.planwith.user.application.port.in.FollowMemberUseCase;
import com.planwith.user.application.port.in.ListFollowUseCase;
import com.planwith.user.application.port.out.FollowPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService implements FollowMemberUseCase, ListFollowUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final FollowPort followPort;
    private final GradeService gradeService;

    @Override
    @Transactional
    public void follow(Long followerMemberId, String followeeMemberUuid) {
        User follower = userRepositoryPort.findActiveById(followerMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User followee = userRepositoryPort.findActiveByMemberUuid(followeeMemberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (follower.getMemberUuid().equals(followee.getMemberUuid())) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        Optional<FollowPort.FollowRelation> existing =
                followPort.find(follower.getMemberUuid(), followee.getMemberUuid());
        if (existing.isPresent() && existing.get().active()) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
        }

        FollowPort.FollowRelation previous = existing.orElse(null);
        followPort.save(new FollowPort.FollowRelation(
                previous != null ? previous.followId() : null,
                previous != null ? previous.followUuid() : UUID.randomUUID().toString(),
                follower.getMemberUuid(),
                followee.getMemberUuid(),
                true
        ));
        gradeService.syncFollowerMetric(followee.getMemberUuid());
    }

    @Override
    @Transactional
    public void unfollow(Long followerMemberId, String followeeMemberUuid) {
        User follower = userRepositoryPort.findActiveById(followerMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepositoryPort.findActiveByMemberUuid(followeeMemberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        FollowPort.FollowRelation relation = followPort.find(follower.getMemberUuid(), followeeMemberUuid)
                .filter(FollowPort.FollowRelation::active)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOLLOWING));

        followPort.save(new FollowPort.FollowRelation(
                relation.followId(),
                relation.followUuid(),
                relation.followerMemberUuid(),
                relation.followeeMemberUuid(),
                false
        ));
        gradeService.syncFollowerMetric(followeeMemberUuid);
    }

    @Override
    public List<MemberProfileInfo> listFollowers(String memberUuid) {
        ensureMemberExists(memberUuid);
        return followPort.findFollowerUuids(memberUuid).stream()
                .map(this::toSummary)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public List<MemberProfileInfo> listFollowing(String memberUuid) {
        ensureMemberExists(memberUuid);
        return followPort.findFollowingUuids(memberUuid).stream()
                .map(this::toSummary)
                .flatMap(Optional::stream)
                .toList();
    }

    private void ensureMemberExists(String memberUuid) {
        userRepositoryPort.findActiveByMemberUuid(memberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Optional<MemberProfileInfo> toSummary(String uuid) {
        return userRepositoryPort.findActiveByMemberUuid(uuid)
                .map(user -> MemberProfileInfo.builder()
                        .memberUuid(user.getMemberUuid())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .profileIntro(user.getIntroduction())
                        .grade(user.getGrade())
                        .followerCount(followPort.countFollowers(user.getMemberUuid()))
                        .followingCount(followPort.countFollowing(user.getMemberUuid()))
                        .build());
    }
}
