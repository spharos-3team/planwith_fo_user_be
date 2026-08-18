package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.grade.GradeMetricType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_grade_metric",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_grade_metric", columnNames = {"member_uuid", "metric_type"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGradeMetricJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private GradeMetricType metricType;

    @Column(name = "current_value", nullable = false)
    private long currentValue;

    @Column(name = "source_service", nullable = false, length = 50)
    private String sourceService;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

    @Column(name = "synchronized_at", nullable = false)
    private LocalDateTime synchronizedAt;

    @Builder
    public MemberGradeMetricJpaEntity(Long metricId, String memberUuid, GradeMetricType metricType, long currentValue,
                                      String sourceService, long sourceVersion, LocalDateTime synchronizedAt) {
        this.metricId = metricId;
        this.memberUuid = memberUuid;
        this.metricType = metricType;
        this.currentValue = currentValue;
        this.sourceService = sourceService;
        this.sourceVersion = sourceVersion;
        this.synchronizedAt = synchronizedAt;
    }

    public void synchronize(long value, String sourceService, long sourceVersion, LocalDateTime at) {
        this.currentValue = value;
        this.sourceService = sourceService;
        this.sourceVersion = sourceVersion;
        this.synchronizedAt = at;
    }
}
