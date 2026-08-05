package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.GradeBenefitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeBenefitJpaRepository extends JpaRepository<GradeBenefitJpaEntity, Long> {

    List<GradeBenefitJpaEntity> findByGradeId(Long gradeId);

    List<GradeBenefitJpaEntity> findByGradeIdIn(List<Long> gradeIds);

    boolean existsByGradeId(Long gradeId);
}
