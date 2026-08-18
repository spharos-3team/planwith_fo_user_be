package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.GradeRewardHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRewardHistoryJpaRepository extends JpaRepository<GradeRewardHistoryJpaEntity, Long> {

    boolean existsByMemberUuidAndRewardMonth(String memberUuid, String rewardMonth);

    List<GradeRewardHistoryJpaEntity> findByMemberUuidOrderByCreatedAtDesc(String memberUuid);
}
