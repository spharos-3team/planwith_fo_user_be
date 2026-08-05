package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.FollowJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, Long> {

    Optional<FollowJpaEntity> findByFollowerMemberUuidAndFolloweeMemberUuid(
            String followerMemberUuid, String followeeMemberUuid);

    long countByFolloweeMemberUuidAndActiveTrue(String followeeMemberUuid);

    long countByFollowerMemberUuidAndActiveTrue(String followerMemberUuid);

    List<FollowJpaEntity> findByFolloweeMemberUuidAndActiveTrueOrderByFollowIdDesc(String followeeMemberUuid);

    List<FollowJpaEntity> findByFollowerMemberUuidAndActiveTrueOrderByFollowIdDesc(String followerMemberUuid);
}
