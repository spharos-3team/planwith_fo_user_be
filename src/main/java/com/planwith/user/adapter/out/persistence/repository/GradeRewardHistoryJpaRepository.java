package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.GradeRewardHistoryJpaEntity;
import com.planwith.user.domain.grade.GradeRewardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRewardHistoryJpaRepository extends JpaRepository<GradeRewardHistoryJpaEntity, Long> {

    boolean existsByMemberIdAndRewardTypeAndPeriodYm(Long memberId, GradeRewardType rewardType, String periodYm);

    List<GradeRewardHistoryJpaEntity> findByMemberIdOrderByGrantedAtDesc(Long memberId);
}
