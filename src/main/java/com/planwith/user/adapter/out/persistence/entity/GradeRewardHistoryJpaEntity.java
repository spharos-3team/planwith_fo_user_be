package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.grade.GradeRewardStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "grade_reward_history",
        uniqueConstraints = @UniqueConstraint(name = "uk_grade_reward_month", columnNames = {"member_uuid", "reward_month"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeRewardHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long rewardId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(name = "reward_month", nullable = false, length = 7)
    private String rewardMonth;

    @Column(name = "token_amount", nullable = false)
    private Integer tokenAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_status", nullable = false, length = 20)
    private GradeRewardStatus rewardStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public GradeRewardHistoryJpaEntity(Long rewardId, String memberUuid, Long gradeId, String rewardMonth,
                                       Integer tokenAmount, GradeRewardStatus rewardStatus, LocalDateTime createdAt) {
        this.rewardId = rewardId;
        this.memberUuid = memberUuid;
        this.gradeId = gradeId;
        this.rewardMonth = rewardMonth;
        this.tokenAmount = tokenAmount;
        this.rewardStatus = rewardStatus;
        this.createdAt = createdAt;
    }
}
