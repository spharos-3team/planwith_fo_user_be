package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.grade.GradeMemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grade_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeMemberJpaEntity {

    @Id
    @Column(name = "member_uuid", length = 36)
    private String memberUuid;

    @Column(name = "grade_uuid", nullable = false, unique = true, length = 36)
    private String gradeUuid;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_status", nullable = false, length = 20)
    private GradeMemberStatus gradeStatus;

    @Column(name = "grade_assigned_at", nullable = false)
    private LocalDateTime gradeAssignedAt;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @Builder
    public GradeMemberJpaEntity(String memberUuid, String gradeUuid, Long gradeId, GradeMemberStatus gradeStatus,
                                LocalDateTime gradeAssignedAt, LocalDateTime lastEvaluatedAt) {
        this.memberUuid = memberUuid;
        this.gradeUuid = gradeUuid;
        this.gradeId = gradeId;
        this.gradeStatus = gradeStatus;
        this.gradeAssignedAt = gradeAssignedAt;
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public void changeGrade(Long gradeId, LocalDateTime assignedAt) {
        this.gradeId = gradeId;
        this.gradeAssignedAt = assignedAt;
        this.gradeStatus = GradeMemberStatus.ACTIVE;
    }

    public void markEvaluated(LocalDateTime evaluatedAt) {
        this.lastEvaluatedAt = evaluatedAt;
    }
}
