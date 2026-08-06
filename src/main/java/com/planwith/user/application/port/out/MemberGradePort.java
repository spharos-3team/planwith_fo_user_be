package com.planwith.user.application.port.out;

import com.planwith.user.application.dto.GradeCatalogItem;
import com.planwith.user.application.dto.GradeRewardView;
import com.planwith.user.application.dto.MemberGradeView;
import com.planwith.user.domain.grade.MemberGradeCode;

import java.util.List;
import java.util.Optional;

public interface MemberGradePort {

    List<GradeCatalogItem> listCatalog();

    Optional<GradeCatalogItem> findCatalogByCode(MemberGradeCode code);

    void initializeMember(Long memberId, String memberUuid);

    MemberGradeView getMemberGradeByUuid(String memberUuid);

    MemberGradeCode getCurrentGradeCode(String memberUuid);

    void upsertMetric(String memberUuid, String metricType, long value, String sourceService);

    long getMetricValue(String memberUuid, String metricType);

    void updateMemberGrade(String memberUuid, MemberGradeCode gradeCode);

    void markEvaluated(String memberUuid);

    void mirrorProfileGrade(Long memberId, MemberGradeCode gradeCode);

    boolean rewardExists(String memberUuid, String rewardMonth);

    void saveMonthlyReward(String memberUuid, MemberGradeCode gradeCode, int tokenAmount, String rewardMonth);

    int monthlyTokenAmount(MemberGradeCode gradeCode);

    List<GradeRewardView> listRewards(String memberUuid);

    List<MemberGradeAssignment> listAssignmentsForActiveMembers();

    record MemberGradeAssignment(String memberUuid, MemberGradeCode gradeCode) {
    }
}
