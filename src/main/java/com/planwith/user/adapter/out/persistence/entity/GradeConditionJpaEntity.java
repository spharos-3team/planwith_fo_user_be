package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.grade.GradeMetricType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "grade_condition",
        uniqueConstraints = @UniqueConstraint(name = "uk_grade_condition", columnNames = {"grade_id", "metric_type"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeConditionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long conditionId;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private GradeMetricType metricType;

    @Column(name = "condition_name", nullable = false, length = 100)
    private String conditionName;

    @Column(name = "threshold_value", nullable = false)
    private Long thresholdValue;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    public GradeConditionJpaEntity(Long conditionId, Long gradeId, GradeMetricType metricType, String conditionName,
                                   Long thresholdValue, Integer sortOrder, String description) {
        this.conditionId = conditionId;
        this.gradeId = gradeId;
        this.metricType = metricType;
        this.conditionName = conditionName;
        this.thresholdValue = thresholdValue;
        this.sortOrder = sortOrder;
        this.description = description;
    }
}
