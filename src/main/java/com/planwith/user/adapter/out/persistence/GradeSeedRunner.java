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
                            .nameKo(code.nameKo())
                            .sortOrder(code.sortOrder())
                            .monthlyTokenAmount(code.monthlyTokenAmount())
                            .build()));

            if (!gradeConditionJpaRepository.existsByGradeId(grade.getGradeId())) {
                gradeConditionJpaRepository.saveAll(List.of(
                        condition(grade.getGradeId(), GradeMetricType.STORY, code.minStories()),
                        condition(grade.getGradeId(), GradeMetricType.FOLLOWER, code.minFollowers()),
                        condition(grade.getGradeId(), GradeMetricType.LIKE, code.minLikes())
                ));
            }

            if (!gradeBenefitJpaRepository.existsByGradeId(grade.getGradeId())) {
                gradeBenefitJpaRepository.saveAll(benefitsFor(grade.getGradeId(), code));
            }
        }
        log.info("Grade master seed ensured for {} grades", MemberGradeCode.values().length);
    }

    private static GradeConditionJpaEntity condition(Long gradeId, GradeMetricType type, long threshold) {
        return GradeConditionJpaEntity.builder()
                .gradeId(gradeId)
                .metricType(type)
                .thresholdValue(threshold)
                .build();
    }

    private static List<GradeBenefitJpaEntity> benefitsFor(Long gradeId, MemberGradeCode code) {
        List<GradeBenefitJpaEntity> benefits = new ArrayList<>();
        benefits.add(benefit(gradeId, GradeBenefitCode.MONTHLY_TOKEN,
                "매월 " + code.monthlyTokenAmount() + " 토큰 부여"));
        if (code.sortOrder() >= MemberGradeCode.EXPLORER.sortOrder()) {
            benefits.add(benefit(gradeId, GradeBenefitCode.PROFILE_BADGE, "프로필 배지 지급"));
        }
        if (code == MemberGradeCode.EXPLORER) {
            benefits.add(benefit(gradeId, GradeBenefitCode.MEMBERSHIP_STORY, "멤버십 회원공개 스토리 작성 가능"));
        }
        if (code.sortOrder() >= MemberGradeCode.ADVENTURER.sortOrder()) {
            benefits.add(benefit(gradeId, GradeBenefitCode.SPECIAL_BORDER, "프로필 특별 테두리"));
            benefits.add(benefit(gradeId, GradeBenefitCode.STORY_PRIORITY,
                    code == MemberGradeCode.MASTER
                            ? "비회원 스토리 진입 시 우선 노출 (마스터)"
                            : "비회원 스토리 진입 시 우선 노출 (모험가)"));
        }
        return benefits;
    }

    private static GradeBenefitJpaEntity benefit(Long gradeId, GradeBenefitCode code, String description) {
        return GradeBenefitJpaEntity.builder()
                .gradeId(gradeId)
                .benefitCode(code)
                .description(description)
                .build();
    }
}
