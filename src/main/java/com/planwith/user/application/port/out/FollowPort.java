package com.planwith.user.application.port.out;

import java.util.List;
import java.util.Optional;

public interface FollowPort {

    Optional<FollowRelation> find(String followerMemberUuid, String followeeMemberUuid);

    FollowRelation save(FollowRelation relation);

    long countFollowers(String memberUuid);

    long countFollowing(String memberUuid);

    List<String> findFollowerUuids(String memberUuid);

    List<String> findFollowingUuids(String memberUuid);

    record FollowRelation(
            Long followId,
            String followUuid,
            String followerMemberUuid,
            String followeeMemberUuid,
            boolean active
    ) {
    }
}
