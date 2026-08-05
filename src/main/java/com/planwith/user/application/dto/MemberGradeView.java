package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemberGradeView {

    private final String memberUuid;
    private final String gradeCode;
    private final String nameKo;
    private final int sortOrder;
    private final int monthlyTokenAmount;
    private final LocalDateTime gradedAt;
    private final Metrics metrics;
    private final List<GradeCatalogItem.Benefit> benefits;

    @Getter
    @Builder
    public static class Metrics {
        private final long storyCount;
        private final long followerCount;
        private final long likeCount;
        private final LocalDateTime metricsUpdatedAt;
    }
}
