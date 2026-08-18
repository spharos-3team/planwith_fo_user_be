package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.MemberGradeMetricJpaEntity;
import com.planwith.user.domain.grade.GradeMetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGradeMetricJpaRepository extends JpaRepository<MemberGradeMetricJpaEntity, Long> {

    List<MemberGradeMetricJpaEntity> findByMemberUuid(String memberUuid);

    Optional<MemberGradeMetricJpaEntity> findByMemberUuidAndMetricType(String memberUuid, GradeMetricType metricType);

    boolean existsByMemberUuid(String memberUuid);
}
