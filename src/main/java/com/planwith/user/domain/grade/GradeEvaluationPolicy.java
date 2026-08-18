package com.planwith.user.domain.grade;

import java.util.Comparator;

/**
 * Picks the highest grade whose story/follower/like thresholds are all met.
 * Never demotes below the current grade.
 */
public final class GradeEvaluationPolicy {

    private GradeEvaluationPolicy() {
    }

    public static MemberGradeCode highestSatisfied(
            long storyCount,
            long followerCount,
            long likeCount,
            MemberGradeCode currentOrNull
    ) {
        MemberGradeCode highest = MemberGradeCode.orderedAscending().stream()
                .filter(code -> code.isSatisfiedBy(storyCount, followerCount, likeCount))
                .max(Comparator.comparingInt(MemberGradeCode::sortOrder))
                .orElse(MemberGradeCode.ROOKIE);

        if (currentOrNull == null) {
            return highest;
        }
        return highest.sortOrder() >= currentOrNull.sortOrder() ? highest : currentOrNull;
    }
}
