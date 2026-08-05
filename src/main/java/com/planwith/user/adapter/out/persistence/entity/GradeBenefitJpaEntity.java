package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.grade.GradeBenefitCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "grade_benefit",
        uniqueConstraints = @UniqueConstraint(name = "uk_grade_benefit", columnNames = {"grade_id", "benefit_code"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeBenefitJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "benefit_id")
    private Long benefitId;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_code", nullable = false, length = 40)
    private GradeBenefitCode benefitCode;

    @Column(nullable = false, length = 200)
    private String description;

    @Builder
    public GradeBenefitJpaEntity(Long benefitId, Long gradeId, GradeBenefitCode benefitCode, String description) {
        this.benefitId = benefitId;
        this.gradeId = gradeId;
        this.benefitCode = benefitCode;
        this.description = description;
    }
}
