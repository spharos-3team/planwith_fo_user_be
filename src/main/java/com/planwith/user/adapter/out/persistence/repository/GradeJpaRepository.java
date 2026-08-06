package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.GradeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeJpaRepository extends JpaRepository<GradeJpaEntity, Long> {

    Optional<GradeJpaEntity> findByGradeCode(String gradeCode);

    List<GradeJpaEntity> findAllByOrderByGradeLevelAsc();
}
