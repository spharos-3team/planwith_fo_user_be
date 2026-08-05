package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGradeJpaEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    @Builder
    public MemberGradeJpaEntity(Long memberId, String memberUuid, Long gradeId, LocalDateTime gradedAt) {
        this.memberId = memberId;
        this.memberUuid = memberUuid;
        this.gradeId = gradeId;
        this.gradedAt = gradedAt;
    }

    public void changeGrade(Long gradeId, LocalDateTime gradedAt) {
        this.gradeId = gradeId;
        this.gradedAt = gradedAt;
    }
}
