package com.planwith.user.application.service;

import com.planwith.user.application.dto.GradeCatalogItem;
import com.planwith.user.application.dto.GradeEvaluateResult;
import com.planwith.user.application.dto.GradeRewardView;
import com.planwith.user.application.dto.MemberGradeView;
import com.planwith.user.application.port.in.EvaluateMemberGradeUseCase;
import com.planwith.user.application.port.in.GetMemberGradeUseCase;
import com.planwith.user.application.port.in.GrantMonthlyGradeRewardsUseCase;
import com.planwith.user.application.port.in.ListGradeRewardsUseCase;
import com.planwith.user.application.port.in.ListGradesUseCase;
import com.planwith.user.application.port.out.FollowPort;
import com.planwith.user.application.port.out.MemberGradePort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.grade.GradeEvaluationPolicy;
import com.planwith.user.domain.grade.MemberGradeCode;
import com.planwith.user.domain.user.User;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService implements
        ListGradesUseCase,
        GetMemberGradeUseCase,
        EvaluateMemberGradeUseCase,
        GrantMonthlyGradeRewardsUseCase,
        ListGradeRewardsUseCase {

    private static final DateTimeFormatter PERIOD = DateTimeFormatter.ofPattern("yyyy-MM");

    private final MemberGradePort memberGradePort;
    private final UserRepositoryPort userRepositoryPort;
    private final FollowPort followPort;

    @Override
    public List<GradeCatalogItem> listGrades() {
        return memberGradePort.listCatalog();
    }

    @Override
    public MemberGradeView getMyGrade(Long memberId) {
        return memberGradePort.getMemberGrade(memberId);
    }

    @Override
    public MemberGradeView getByMemberUuid(String memberUuid) {
        return memberGradePort.getMemberGradeByUuid(memberUuid);
    }

    @Override
    @Transactional
    public GradeEvaluateResult evaluate(String memberUuid, long storyCount, long likeCount) {
        if (!StringUtils.hasText(memberUuid)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        if (storyCount < 0 || likeCount < 0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        User user = userRepositoryPort.findActiveByMemberUuid(memberUuid.trim())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        long followerCount = followPort.countFollowers(user.getMemberUuid());
        MemberGradeCode previous = memberGradePort.getCurrentGradeCode(user.getId());

        memberGradePort.saveMetrics(user.getId(), user.getMemberUuid(), storyCount, followerCount, likeCount);

        MemberGradeCode next = GradeEvaluationPolicy.highestSatisfied(
                storyCount, followerCount, likeCount, previous);
        if (next.sortOrder() > previous.sortOrder()) {
            memberGradePort.updateMemberGrade(user.getId(), user.getMemberUuid(), next);
        }

        MemberGradeView view = memberGradePort.getMemberGrade(user.getId());
        return GradeEvaluateResult.builder()
                .memberUuid(user.getMemberUuid())
                .previousGradeCode(previous.name())
                .currentGradeCode(view.getGradeCode())
                .upgraded(!previous.name().equals(view.getGradeCode()))
                .metrics(view.getMetrics())
                .build();
    }

    @Override
    @Transactional
    public int grantForPeriod(String periodYm) {
        String period = resolvePeriod(periodYm);
        int created = 0;
        for (MemberGradePort.MemberGradeAssignment assignment : memberGradePort.listAssignmentsForActiveMembers()) {
            if (memberGradePort.rewardExists(assignment.memberId(), period)) {
                continue;
            }
            memberGradePort.saveMonthlyReward(
                    assignment.memberId(),
                    assignment.gradeCode(),
                    assignment.gradeCode().monthlyTokenAmount(),
                    period
            );
            created++;
        }
        return created;
    }

    @Override
    public List<GradeRewardView> listMyRewards(Long memberId) {
        return memberGradePort.listRewards(memberId);
    }

    @Transactional
    public void syncFollowerMetric(String memberUuid) {
        User user = userRepositoryPort.findActiveByMemberUuid(memberUuid)
                .orElse(null);
        if (user == null) {
            return;
        }
        long followers = followPort.countFollowers(memberUuid);
        memberGradePort.updateFollowerCount(user.getId(), user.getMemberUuid(), followers);
    }

    private static String resolvePeriod(String periodYm) {
        if (!StringUtils.hasText(periodYm)) {
            return YearMonth.now().format(PERIOD);
        }
        try {
            return YearMonth.parse(periodYm.trim(), PERIOD).format(PERIOD);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
