package com.planwith.user.domain.grade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradeEvaluationPolicyTest {

    @Test
    @DisplayName("defaults to ROOKIE when no thresholds met beyond baseline")
    void highest_rookie() {
        assertThat(GradeEvaluationPolicy.highestSatisfied(0, 0, 0, null))
                .isEqualTo(MemberGradeCode.ROOKIE);
    }

    @Test
    @DisplayName("promotes to LEAF at exact boundary 3/10/30")
    void highest_leafBoundary() {
        assertThat(GradeEvaluationPolicy.highestSatisfied(3, 10, 30, MemberGradeCode.ROOKIE))
                .isEqualTo(MemberGradeCode.LEAF);
    }

    @Test
    @DisplayName("does not promote when one metric is below threshold")
    void highest_notLeafWhenFollowersLow() {
        assertThat(GradeEvaluationPolicy.highestSatisfied(3, 9, 30, MemberGradeCode.ROOKIE))
                .isEqualTo(MemberGradeCode.ROOKIE);
    }

    @Test
    @DisplayName("never demotes below current grade")
    void highest_noDemotion() {
        assertThat(GradeEvaluationPolicy.highestSatisfied(0, 0, 0, MemberGradeCode.TRAVELER))
                .isEqualTo(MemberGradeCode.TRAVELER);
    }

    @Test
    @DisplayName("selects highest satisfied grade")
    void highest_explorer() {
        assertThat(GradeEvaluationPolicy.highestSatisfied(30, 1_000, 5_000, MemberGradeCode.LEAF))
                .isEqualTo(MemberGradeCode.EXPLORER);
    }
}
