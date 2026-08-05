package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.FollowJpaEntity;
import com.planwith.user.adapter.out.persistence.repository.FollowJpaRepository;
import com.planwith.user.application.port.out.FollowPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowPersistenceAdapter implements FollowPort {

    private final FollowJpaRepository followJpaRepository;

    @Override
    public Optional<FollowRelation> find(String followerMemberUuid, String followeeMemberUuid) {
        return followJpaRepository
                .findByFollowerMemberUuidAndFolloweeMemberUuid(followerMemberUuid, followeeMemberUuid)
                .map(this::toRelation);
    }

    @Override
    @Transactional
    public FollowRelation save(FollowRelation relation) {
        FollowJpaEntity entity = followJpaRepository
                .findByFollowerMemberUuidAndFolloweeMemberUuid(
                        relation.followerMemberUuid(), relation.followeeMemberUuid())
                .orElseGet(() -> FollowJpaEntity.builder()
                        .followUuid(relation.followUuid() != null
                                ? relation.followUuid()
                                : UUID.randomUUID().toString())
                        .followerMemberUuid(relation.followerMemberUuid())
                        .followeeMemberUuid(relation.followeeMemberUuid())
                        .active(relation.active())
                        .build());

        if (relation.active()) {
            entity.activate();
        } else {
            entity.deactivate();
        }
        return toRelation(followJpaRepository.save(entity));
    }

    @Override
    public long countFollowers(String memberUuid) {
        return followJpaRepository.countByFolloweeMemberUuidAndActiveTrue(memberUuid);
    }

    @Override
    public long countFollowing(String memberUuid) {
        return followJpaRepository.countByFollowerMemberUuidAndActiveTrue(memberUuid);
    }

    @Override
    public List<String> findFollowerUuids(String memberUuid) {
        return followJpaRepository.findByFolloweeMemberUuidAndActiveTrueOrderByFollowIdDesc(memberUuid).stream()
                .map(FollowJpaEntity::getFollowerMemberUuid)
                .toList();
    }

    @Override
    public List<String> findFollowingUuids(String memberUuid) {
        return followJpaRepository.findByFollowerMemberUuidAndActiveTrueOrderByFollowIdDesc(memberUuid).stream()
                .map(FollowJpaEntity::getFolloweeMemberUuid)
                .toList();
    }

    private FollowRelation toRelation(FollowJpaEntity entity) {
        return new FollowRelation(
                entity.getFollowId(),
                entity.getFollowUuid(),
                entity.getFollowerMemberUuid(),
                entity.getFolloweeMemberUuid(),
                entity.isActive()
        );
    }
}
