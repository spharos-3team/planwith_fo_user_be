package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.MemberProfileJpaEntity;
import com.planwith.user.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberProfileJpaRepository extends JpaRepository<MemberProfileJpaEntity, Long> {

    @Query("""
            select case when count(p) > 0 then true else false end
            from MemberProfileJpaEntity p, MemberJpaEntity m
            where p.memberId = m.memberId
              and p.nickname = :nickname
              and m.status <> :excludedStatus
            """)
    boolean existsActiveByNickname(
            @Param("nickname") String nickname,
            @Param("excludedStatus") UserStatus excludedStatus);

    @Query("""
            select case when count(p) > 0 then true else false end
            from MemberProfileJpaEntity p, MemberJpaEntity m
            where p.memberId = m.memberId
              and p.nickname = :nickname
              and m.status <> :excludedStatus
              and m.memberId <> :memberId
            """)
    boolean existsActiveByNicknameExcludingMemberId(
            @Param("nickname") String nickname,
            @Param("memberId") Long memberId,
            @Param("excludedStatus") UserStatus excludedStatus);

    Optional<MemberProfileJpaEntity> findByMemberUuid(String memberUuid);

    Optional<MemberProfileJpaEntity> findByNickname(String nickname);
}
