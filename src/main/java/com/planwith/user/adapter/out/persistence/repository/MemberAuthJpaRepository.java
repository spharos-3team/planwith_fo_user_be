package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.MemberAuthJpaEntity;
import com.planwith.user.domain.user.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAuthJpaRepository extends JpaRepository<MemberAuthJpaEntity, Long> {

    Optional<MemberAuthJpaEntity> findByLoginTypeAndEmail(LoginType loginType, String email);

    Optional<MemberAuthJpaEntity> findByLoginTypeAndSocialId(LoginType loginType, String socialId);

    Optional<MemberAuthJpaEntity> findByMemberId(Long memberId);
}
