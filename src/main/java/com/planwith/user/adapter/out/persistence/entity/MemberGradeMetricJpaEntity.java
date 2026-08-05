package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_grade_metric")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGradeMetricJpaEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Column(name = "story_count", nullable = false)
    private long storyCount;

    @Column(name = "follower_count", nullable = false)
    private long followerCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "metrics_updated_at", nullable = false)
    private LocalDateTime metricsUpdatedAt;

    @Builder
    public MemberGradeMetricJpaEntity(Long memberId, String memberUuid, long storyCount, long followerCount,
                                      long likeCount, LocalDateTime metricsUpdatedAt) {
        this.memberId = memberId;
        this.memberUuid = memberUuid;
        this.storyCount = storyCount;
        this.followerCount = followerCount;
        this.likeCount = likeCount;
        this.metricsUpdatedAt = metricsUpdatedAt;
    }

    public void updateCounts(long storyCount, long followerCount, long likeCount, LocalDateTime at) {
        this.storyCount = storyCount;
        this.followerCount = followerCount;
        this.likeCount = likeCount;
        this.metricsUpdatedAt = at;
    }

    public void updateFollowerCount(long followerCount, LocalDateTime at) {
        this.followerCount = followerCount;
        this.metricsUpdatedAt = at;
    }
}
