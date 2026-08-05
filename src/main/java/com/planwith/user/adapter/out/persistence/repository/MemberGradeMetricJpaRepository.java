package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.MemberGradeMetricJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberGradeMetricJpaRepository extends JpaRepository<MemberGradeMetricJpaEntity, Long> {

    Optional<MemberGradeMetricJpaEntity> findByMemberUuid(String memberUuid);
}
