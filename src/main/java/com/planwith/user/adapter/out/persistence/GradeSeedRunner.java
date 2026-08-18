package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.GradeBenefitJpaEntity;
import com.planwith.user.adapter.out.persistence.entity.GradeConditionJpaEntity;
import com.planwith.user.adapter.out.persistence.entity.GradeJpaEntity;
import com.planwith.user.adapter.out.persistence.repository.GradeBenefitJpaRepository;
import com.planwith.user.adapter.out.persistence.repository.GradeConditionJpaRepository;
import com.planwith.user.adapter.out.persistence.repository.GradeJpaRepository;
import com.planwith.user.domain.grade.GradeBenefitCode;
import com.planwith.user.domain.grade.GradeMetricType;
import com.planwith.user.domain.grade.MemberGradeCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class GradeSeedRunner implements ApplicationRunner {

    private final GradeJpaRepository gradeJpaRepository;
    private final GradeConditionJpaRepository gradeConditionJpaRepository;
    private final GradeBenefitJpaRepository gradeBenefitJpaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (MemberGradeCode code : MemberGradeCode.orderedAscending()) {
            GradeJpaEntity grade = gradeJpaRepository.findByGradeCode(code.name())
                    .orElseGet(() -> gradeJpaRepository.save(GradeJpaEntity.builder()
                            .gradeCode(code.name())
                            .gradeName(code.nameKo())
                            .gradeLevel(code.sortOrder())
                            .description(code.nameKo() + " 등급")
                            .build()));

            if (!gradeConditionJpaRepository.existsByGradeId(grade.getGradeId())) {
                gradeConditionJpaRepository.saveAll(List.of(
                        condition(grade.getGradeId(), GradeMetricType.STORY, "스토리 수", code.minStories(), 1),
                        condition(grade.getGradeId(), GradeMetricType.FOLLOWER, "팔로워 수", code.minFollowers(), 2),
                        condition(grade.getGradeId(), GradeMetricType.LIKE, "누적 좋아요", code.minLikes(), 3)
                ));
            }

            if (!gradeBenefitJpaRepository.existsByGradeId(grade.getGradeId())) {
                gradeBenefitJpaRepository.saveAll(benefitsFor(grade.getGradeId(), code));
            }
        }
        log.info("Grade master seed ensured for {} grades", MemberGradeCode.values().length);
    }

    private static GradeConditionJpaEntity condition(
            Long gradeId, GradeMetricType type, String name, long threshold, int sortOrder
    ) {
        return GradeConditionJpaEntity.builder()
                .gradeId(gradeId)
                .metricType(type)
                .conditionName(name)
                .thresholdValue(threshold)
                .sortOrder(sortOrder)
                .description(name + " " + threshold + " 이상")
                .build();
    }

    private static List<GradeBenefitJpaEntity> benefitsFor(Long gradeId, MemberGradeCode code) {
        List<GradeBenefitJpaEntity> benefits = new ArrayList<>();
        benefits.add(benefit(gradeId, GradeBenefitCode.MONTHLY_TOKEN, "월간 토큰",
                String.valueOf(code.monthlyTokenAmount()), "매월 " + code.monthlyTokenAmount() + " 토큰 부여", 1));
        if (code.sortOrder() >= MemberGradeCode.EXPLORER.sortOrder()) {
            benefits.add(benefit(gradeId, GradeBenefitCode.PROFILE_BADGE, "프로필 배지", "true", "프로필 배지 지급", 2));
        }
        if (code == MemberGradeCode.EXPLORER) {
            benefits.add(benefit(gradeId, GradeBenefitCode.MEMBERSHIP_STORY, "멤버십 스토리", "true",
                    "멤버십 회원공개 스토리 작성 가능", 3));
        }
        if (code.sortOrder() >= MemberGradeCode.ADVENTURER.sortOrder()) {
            benefits.add(benefit(gradeId, GradeBenefitCode.SPECIAL_BORDER, "특별 테두리", "true", "프로필 특별 테두리", 3));
            benefits.add(benefit(gradeId, GradeBenefitCode.STORY_PRIORITY, "스토리 우선 노출", code.name(),
                    code == MemberGradeCode.MASTER
                            ? "비회원 스토리 진입 시 우선 노출 (마스터)"
                            : "비회원 스토리 진입 시 우선 노출 (모험가)", 4));
        }
        return benefits;
    }

    private static GradeBenefitJpaEntity benefit(
            Long gradeId, GradeBenefitCode code, String name, String value, String description, int sortOrder
    ) {
        return GradeBenefitJpaEntity.builder()
                .gradeId(gradeId)
                .benefitCode(code)
                .benefitName(name)
                .benefitValue(value)
                .description(description)
                .sortOrder(sortOrder)
                .build();
    }
}
