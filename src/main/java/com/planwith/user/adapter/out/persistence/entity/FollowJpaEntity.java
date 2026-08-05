package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "follow",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_follow_follower_followee",
                columnNames = {"follower_member_uuid", "followee_member_uuid"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long followId;

    @Column(name = "follow_uuid", nullable = false, unique = true, length = 36)
    private String followUuid;

    @Column(name = "follower_member_uuid", nullable = false, length = 36)
    private String followerMemberUuid;

    @Column(name = "followee_member_uuid", nullable = false, length = 36)
    private String followeeMemberUuid;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Builder
    public FollowJpaEntity(Long followId, String followUuid, String followerMemberUuid,
                           String followeeMemberUuid, Boolean active) {
        this.followId = followId;
        this.followUuid = followUuid;
        this.followerMemberUuid = followerMemberUuid;
        this.followeeMemberUuid = followeeMemberUuid;
        this.active = active == null || active;
    }
}
