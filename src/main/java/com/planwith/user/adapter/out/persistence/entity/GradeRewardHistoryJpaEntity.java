package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.grade.GradeRewardType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "grade_reward_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_grade_reward_uuid", columnNames = "reward_uuid"),
                @UniqueConstraint(name = "uk_grade_reward_period", columnNames = {"member_id", "reward_type", "period_ym"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeRewardHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long rewardId;

    @Column(name = "reward_uuid", nullable = false, length = 36)
    private String rewardUuid;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 40)
    private GradeRewardType rewardType;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "period_ym", nullable = false, length = 7)
    private String periodYm;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Builder
    public GradeRewardHistoryJpaEntity(Long rewardId, String rewardUuid, Long memberId, Long gradeId,
                                       GradeRewardType rewardType, Integer amount, String periodYm,
                                       LocalDateTime grantedAt) {
        this.rewardId = rewardId;
        this.rewardUuid = rewardUuid;
        this.memberId = memberId;
        this.gradeId = gradeId;
        this.rewardType = rewardType;
        this.amount = amount;
        this.periodYm = periodYm;
        this.grantedAt = grantedAt;
    }
}
