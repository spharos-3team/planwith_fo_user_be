package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "grade_code", nullable = false, unique = true, length = 30)
    private String gradeCode;

    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;

    @Column(name = "sort_order", nullable = false, unique = true)
    private Integer sortOrder;

    @Column(name = "monthly_token_amount", nullable = false)
    private Integer monthlyTokenAmount;

    @Builder
    public GradeJpaEntity(Long gradeId, String gradeCode, String nameKo, Integer sortOrder, Integer monthlyTokenAmount) {
        this.gradeId = gradeId;
        this.gradeCode = gradeCode;
        this.nameKo = nameKo;
        this.sortOrder = sortOrder;
        this.monthlyTokenAmount = monthlyTokenAmount;
    }
}
