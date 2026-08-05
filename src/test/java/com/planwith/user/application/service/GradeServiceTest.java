package com.planwith.user.application.service;

import com.planwith.user.application.dto.GradeEvaluateResult;
import com.planwith.user.application.dto.MemberGradeView;
import com.planwith.user.application.port.out.FollowPort;
import com.planwith.user.application.port.out.MemberGradePort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.grade.MemberGradeCode;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock private MemberGradePort memberGradePort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private FollowPort followPort;
    @InjectMocks private GradeService gradeService;

    @Test
    @DisplayName("evaluate upgrades when metrics satisfy higher grade")
    void evaluate_upgrades() {
        User user = User.builder()
                .id(1L).memberUuid("u1").nickname("n").email("a@b.com")
                .loginType(LoginType.LOCAL).status(UserStatus.ACTIVE).grade("ROOKIE").build();
        given(userRepositoryPort.findActiveByMemberUuid("u1")).willReturn(Optional.of(user));
        given(followPort.countFollowers("u1")).willReturn(10L);
        given(memberGradePort.getCurrentGradeCode(1L)).willReturn(MemberGradeCode.ROOKIE);
        given(memberGradePort.getMemberGrade(1L)).willReturn(MemberGradeView.builder()
                .memberUuid("u1")
                .gradeCode("LEAF")
                .nameKo("잎새")
                .sortOrder(2)
                .monthlyTokenAmount(20)
                .gradedAt(LocalDateTime.now())
                .metrics(MemberGradeView.Metrics.builder()
                        .storyCount(3).followerCount(10).likeCount(30)
                        .metricsUpdatedAt(LocalDateTime.now()).build())
                .benefits(List.of())
                .build());

        GradeEvaluateResult result = gradeService.evaluate("u1", 3, 30);

        assertThat(result.isUpgraded()).isTrue();
        assertThat(result.getPreviousGradeCode()).isEqualTo("ROOKIE");
        assertThat(result.getCurrentGradeCode()).isEqualTo("LEAF");
        verify(memberGradePort).saveMetrics(1L, "u1", 3, 10, 30);
        verify(memberGradePort).updateMemberGrade(1L, "u1", MemberGradeCode.LEAF);
    }

    @Test
    @DisplayName("evaluate does not demote")
    void evaluate_noDemote() {
        User user = User.builder()
                .id(1L).memberUuid("u1").nickname("n").email("a@b.com")
                .loginType(LoginType.LOCAL).status(UserStatus.ACTIVE).grade("TRAVELER").build();
        given(userRepositoryPort.findActiveByMemberUuid("u1")).willReturn(Optional.of(user));
        given(followPort.countFollowers("u1")).willReturn(0L);
        given(memberGradePort.getCurrentGradeCode(1L)).willReturn(MemberGradeCode.TRAVELER);
        given(memberGradePort.getMemberGrade(1L)).willReturn(MemberGradeView.builder()
                .memberUuid("u1")
                .gradeCode("TRAVELER")
                .nameKo("여행가")
                .sortOrder(3)
                .monthlyTokenAmount(30)
                .gradedAt(LocalDateTime.now())
                .metrics(MemberGradeView.Metrics.builder()
                        .storyCount(0).followerCount(0).likeCount(0)
                        .metricsUpdatedAt(LocalDateTime.now()).build())
                .benefits(List.of())
                .build());

        GradeEvaluateResult result = gradeService.evaluate("u1", 0, 0);

        assertThat(result.isUpgraded()).isFalse();
        assertThat(result.getCurrentGradeCode()).isEqualTo("TRAVELER");
        verify(memberGradePort, never()).updateMemberGrade(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("monthly reward is idempotent per member/period")
    void grantMonthly_skipsExisting() {
        given(memberGradePort.listAssignmentsForActiveMembers()).willReturn(List.of(
                new MemberGradePort.MemberGradeAssignment(1L, "u1", MemberGradeCode.ROOKIE),
                new MemberGradePort.MemberGradeAssignment(2L, "u2", MemberGradeCode.LEAF)
        ));
        given(memberGradePort.rewardExists(1L, "2026-08")).willReturn(true);
        given(memberGradePort.rewardExists(2L, "2026-08")).willReturn(false);

        int created = gradeService.grantForPeriod("2026-08");

        assertThat(created).isEqualTo(1);
        verify(memberGradePort).saveMonthlyReward(2L, MemberGradeCode.LEAF, 20, "2026-08");
        verify(memberGradePort, never()).saveMonthlyReward(eq(1L), any(), anyInt(), anyString());
    }
}
