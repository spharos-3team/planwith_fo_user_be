package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.MemberGradeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGradeJpaRepository extends JpaRepository<MemberGradeJpaEntity, Long> {

    Optional<MemberGradeJpaEntity> findByMemberUuid(String memberUuid);

    List<MemberGradeJpaEntity> findAll();
}
