package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.GradeConditionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeConditionJpaRepository extends JpaRepository<GradeConditionJpaEntity, Long> {

    List<GradeConditionJpaEntity> findByGradeIdIn(List<Long> gradeIds);

    boolean existsByGradeId(Long gradeId);
}
