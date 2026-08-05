package com.planwith.user.adapter.in.web.grade.dto;

import com.planwith.user.application.dto.MemberGradeView;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemberGradeResponse {

    private final String memberUuid;
    private final String gradeCode;
    private final String nameKo;
    private final int sortOrder;
    private final int monthlyTokenAmount;
    private final LocalDateTime gradedAt;
    private final MetricsResponse metrics;
    private final List<GradeCatalogResponse.BenefitResponse> benefits;

    public static MemberGradeResponse from(MemberGradeView view) {
        return MemberGradeResponse.builder()
                .memberUuid(view.getMemberUuid())
                .gradeCode(view.getGradeCode())
                .nameKo(view.getNameKo())
                .sortOrder(view.getSortOrder())
                .monthlyTokenAmount(view.getMonthlyTokenAmount())
                .gradedAt(view.getGradedAt())
                .metrics(new MetricsResponse(
                        view.getMetrics().getStoryCount(),
                        view.getMetrics().getFollowerCount(),
                        view.getMetrics().getLikeCount(),
                        view.getMetrics().getMetricsUpdatedAt()
                ))
                .benefits(view.getBenefits().stream()
                        .map(b -> new GradeCatalogResponse.BenefitResponse(b.getBenefitCode(), b.getDescription()))
                        .toList())
                .build();
    }

    public record MetricsResponse(
            long storyCount,
            long followerCount,
            long likeCount,
            LocalDateTime metricsUpdatedAt
    ) {
    }
}
