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

    @Column(name = "grade_name", nullable = false, length = 50)
    private String gradeName;

    @Column(name = "grade_level", nullable = false, unique = true)
    private Integer gradeLevel;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    public GradeJpaEntity(Long gradeId, String gradeCode, String gradeName, Integer gradeLevel, String description) {
        this.gradeId = gradeId;
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.gradeLevel = gradeLevel;
        this.description = description;
    }
}
