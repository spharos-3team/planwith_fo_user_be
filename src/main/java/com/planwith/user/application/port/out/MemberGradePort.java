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

    MemberGradeView getMemberGrade(Long memberId);

    MemberGradeView getMemberGradeByUuid(String memberUuid);

    MemberGradeCode getCurrentGradeCode(Long memberId);

    void saveMetrics(Long memberId, String memberUuid, long storyCount, long followerCount, long likeCount);

    void updateFollowerCount(Long memberId, String memberUuid, long followerCount);

    void updateMemberGrade(Long memberId, String memberUuid, MemberGradeCode gradeCode);

    void mirrorProfileGrade(Long memberId, MemberGradeCode gradeCode);

    boolean rewardExists(Long memberId, String periodYm);

    void saveMonthlyReward(Long memberId, MemberGradeCode gradeCode, int amount, String periodYm);

    List<GradeRewardView> listRewards(Long memberId);

    List<MemberGradeAssignment> listAssignmentsForActiveMembers();

    record MemberGradeAssignment(Long memberId, String memberUuid, MemberGradeCode gradeCode) {
    }
}
