package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.MemberJpaEntity;
import com.planwith.user.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    Optional<MemberJpaEntity> findByMemberIdAndStatusNot(Long memberId, UserStatus status);

    Optional<MemberJpaEntity> findByMemberIdAndStatus(Long memberId, UserStatus status);

    Optional<MemberJpaEntity> findByMemberUuid(String memberUuid);
}
