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
        given(memberGradePort.getCurrentGradeCode("u1")).willReturn(MemberGradeCode.ROOKIE);
        given(memberGradePort.getMemberGradeByUuid("u1")).willReturn(MemberGradeView.builder()
                .memberUuid("u1")
                .gradeUuid("g1")
                .gradeCode("LEAF")
                .gradeName("잎새")
                .gradeLevel(2)
                .gradeStatus("ACTIVE")
                .gradeAssignedAt(LocalDateTime.now())
                .lastEvaluatedAt(LocalDateTime.now())
                .metrics(List.of(
                        MemberGradeView.Metric.builder()
                                .metricType("STORY").currentValue(3)
                                .sourceService("content-be").sourceVersion(1)
                                .synchronizedAt(LocalDateTime.now()).build(),
                        MemberGradeView.Metric.builder()
                                .metricType("FOLLOWER").currentValue(10)
                                .sourceService("fo-user-be").sourceVersion(1)
                                .synchronizedAt(LocalDateTime.now()).build(),
                        MemberGradeView.Metric.builder()
                                .metricType("LIKE").currentValue(30)
                                .sourceService("content-be").sourceVersion(1)
                                .synchronizedAt(LocalDateTime.now()).build()
                ))
                .benefits(List.of())
                .build());

        GradeEvaluateResult result = gradeService.evaluate("u1", 3, 30);

        assertThat(result.isUpgraded()).isTrue();
        assertThat(result.getPreviousGradeCode()).isEqualTo("ROOKIE");
        assertThat(result.getCurrentGradeCode()).isEqualTo("LEAF");
        verify(memberGradePort).upsertMetric("u1", "STORY", 3, "content-be");
        verify(memberGradePort).upsertMetric("u1", "LIKE", 30, "content-be");
        verify(memberGradePort).upsertMetric("u1", "FOLLOWER", 10, "fo-user-be");
        verify(memberGradePort).updateMemberGrade("u1", MemberGradeCode.LEAF);
        verify(memberGradePort).markEvaluated("u1");
    }

    @Test
    @DisplayName("evaluate does not demote")
    void evaluate_noDemote() {
        User user = User.builder()
                .id(1L).memberUuid("u1").nickname("n").email("a@b.com")
                .loginType(LoginType.LOCAL).status(UserStatus.ACTIVE).grade("TRAVELER").build();
        given(userRepositoryPort.findActiveByMemberUuid("u1")).willReturn(Optional.of(user));
        given(followPort.countFollowers("u1")).willReturn(0L);
        given(memberGradePort.getCurrentGradeCode("u1")).willReturn(MemberGradeCode.TRAVELER);
        given(memberGradePort.getMemberGradeByUuid("u1")).willReturn(MemberGradeView.builder()
                .memberUuid("u1")
                .gradeUuid("g1")
                .gradeCode("TRAVELER")
                .gradeName("여행가")
                .gradeLevel(3)
                .gradeStatus("ACTIVE")
                .gradeAssignedAt(LocalDateTime.now())
                .lastEvaluatedAt(LocalDateTime.now())
                .metrics(List.of())
                .benefits(List.of())
                .build());

        GradeEvaluateResult result = gradeService.evaluate("u1", 0, 0);

        assertThat(result.isUpgraded()).isFalse();
        assertThat(result.getCurrentGradeCode()).isEqualTo("TRAVELER");
        verify(memberGradePort, never()).updateMemberGrade(anyString(), any());
    }

    @Test
    @DisplayName("monthly reward is idempotent per member/period")
    void grantMonthly_skipsExisting() {
        given(memberGradePort.listAssignmentsForActiveMembers()).willReturn(List.of(
                new MemberGradePort.MemberGradeAssignment("u1", MemberGradeCode.ROOKIE),
                new MemberGradePort.MemberGradeAssignment("u2", MemberGradeCode.LEAF)
        ));
        given(memberGradePort.rewardExists("u1", "2026-08")).willReturn(true);
        given(memberGradePort.rewardExists("u2", "2026-08")).willReturn(false);
        given(memberGradePort.monthlyTokenAmount(MemberGradeCode.LEAF)).willReturn(20);

        int created = gradeService.grantForPeriod("2026-08");

        assertThat(created).isEqualTo(1);
        verify(memberGradePort).saveMonthlyReward("u2", MemberGradeCode.LEAF, 20, "2026-08");
        verify(memberGradePort, never()).saveMonthlyReward(eq("u1"), any(), anyInt(), anyString());
    }
}
