package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.*;
import com.planwith.user.adapter.out.persistence.repository.*;
import com.planwith.user.application.dto.GradeCatalogItem;
import com.planwith.user.application.dto.GradeRewardView;
import com.planwith.user.application.dto.MemberGradeView;
import com.planwith.user.application.port.out.MemberGradePort;
import com.planwith.user.domain.grade.GradeBenefitCode;
import com.planwith.user.domain.grade.GradeMemberStatus;
import com.planwith.user.domain.grade.GradeMetricType;
import com.planwith.user.domain.grade.GradeRewardStatus;
import com.planwith.user.domain.grade.MemberGradeCode;
import com.planwith.user.domain.user.UserStatus;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradePersistenceAdapter implements MemberGradePort {

    public static final String SOURCE_FO_USER = "fo-user-be";
    public static final String SOURCE_CONTENT = "content-be";

    private final GradeJpaRepository gradeJpaRepository;
    private final GradeConditionJpaRepository gradeConditionJpaRepository;
    private final GradeBenefitJpaRepository gradeBenefitJpaRepository;
    private final GradeMemberJpaRepository gradeMemberJpaRepository;
    private final MemberGradeMetricJpaRepository memberGradeMetricJpaRepository;
    private final GradeRewardHistoryJpaRepository gradeRewardHistoryJpaRepository;
    private final MemberProfileJpaRepository memberProfileJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public List<GradeCatalogItem> listCatalog() {
        return toCatalog(gradeJpaRepository.findAllByOrderByGradeLevelAsc());
    }

    @Override
    public Optional<GradeCatalogItem> findCatalogByCode(MemberGradeCode code) {
        return gradeJpaRepository.findByGradeCode(code.name()).map(g -> toCatalog(List.of(g)).get(0));
    }

    @Override
    @Transactional
    public void initializeMember(Long memberId, String memberUuid) {
        GradeJpaEntity rookie = requireGrade(MemberGradeCode.ROOKIE);
        LocalDateTime now = LocalDateTime.now();
        if (!gradeMemberJpaRepository.existsById(memberUuid)) {
            gradeMemberJpaRepository.save(GradeMemberJpaEntity.builder()
                    .memberUuid(memberUuid)
                    .gradeUuid(UUID.randomUUID().toString())
                    .gradeId(rookie.getGradeId())
                    .gradeStatus(GradeMemberStatus.ACTIVE)
                    .gradeAssignedAt(now)
                    .lastEvaluatedAt(null)
                    .build());
        }
        for (GradeMetricType type : GradeMetricType.values()) {
            if (memberGradeMetricJpaRepository.findByMemberUuidAndMetricType(memberUuid, type).isEmpty()) {
                memberGradeMetricJpaRepository.save(MemberGradeMetricJpaEntity.builder()
                        .memberUuid(memberUuid)
                        .metricType(type)
                        .currentValue(0)
                        .sourceService(SOURCE_FO_USER)
                        .sourceVersion(0)
                        .synchronizedAt(now)
                        .build());
            }
        }
        mirrorProfileGrade(memberId, MemberGradeCode.ROOKIE);
    }

    @Override
    @Transactional
    public MemberGradeView getMemberGradeByUuid(String memberUuid) {
        ensureInitialized(memberUuid);
        return buildView(gradeMemberJpaRepository.findById(memberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    @Transactional
    public MemberGradeCode getCurrentGradeCode(String memberUuid) {
        ensureInitialized(memberUuid);
        Long gradeId = gradeMemberJpaRepository.findById(memberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
                .getGradeId();
        return MemberGradeCode.fromCode(requireGradeEntity(gradeId).getGradeCode());
    }

    @Override
    @Transactional
    public void upsertMetric(String memberUuid, String metricType, long value, String sourceService) {
        ensureInitialized(memberUuid);
        GradeMetricType type = GradeMetricType.valueOf(metricType);
        MemberGradeMetricJpaEntity metric = memberGradeMetricJpaRepository
                .findByMemberUuidAndMetricType(memberUuid, type)
                .orElseGet(() -> MemberGradeMetricJpaEntity.builder()
                        .memberUuid(memberUuid)
                        .metricType(type)
                        .currentValue(0)
                        .sourceService(sourceService)
                        .sourceVersion(0)
                        .synchronizedAt(LocalDateTime.now())
                        .build());
        long nextVersion = metric.getSourceVersion() + 1;
        metric.synchronize(value, sourceService, nextVersion, LocalDateTime.now());
        memberGradeMetricJpaRepository.save(metric);
    }

    @Override
    public long getMetricValue(String memberUuid, String metricType) {
        return memberGradeMetricJpaRepository
                .findByMemberUuidAndMetricType(memberUuid, GradeMetricType.valueOf(metricType))
                .map(MemberGradeMetricJpaEntity::getCurrentValue)
                .orElse(0L);
    }

    @Override
    @Transactional
    public void updateMemberGrade(String memberUuid, MemberGradeCode gradeCode) {
        ensureInitialized(memberUuid);
        GradeJpaEntity grade = requireGrade(gradeCode);
        GradeMemberJpaEntity assignment = gradeMemberJpaRepository.findById(memberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        assignment.changeGrade(grade.getGradeId(), LocalDateTime.now());
        gradeMemberJpaRepository.save(assignment);

        memberJpaRepository.findByMemberUuid(memberUuid).ifPresent(member ->
                mirrorProfileGrade(member.getMemberId(), gradeCode));
    }

    @Override
    @Transactional
    public void markEvaluated(String memberUuid) {
        gradeMemberJpaRepository.findById(memberUuid).ifPresent(assignment -> {
            assignment.markEvaluated(LocalDateTime.now());
            gradeMemberJpaRepository.save(assignment);
        });
    }

    @Override
    @Transactional
    public void mirrorProfileGrade(Long memberId, MemberGradeCode gradeCode) {
        memberProfileJpaRepository.findById(memberId).ifPresent(profile -> {
            profile.apply(profile.getNickname(), profile.getProfileImage(), profile.getProfileIntro(), gradeCode.name());
            memberProfileJpaRepository.save(profile);
        });
    }

    @Override
    public boolean rewardExists(String memberUuid, String rewardMonth) {
        return gradeRewardHistoryJpaRepository.existsByMemberUuidAndRewardMonth(memberUuid, rewardMonth);
    }

    @Override
    @Transactional
    public void saveMonthlyReward(String memberUuid, MemberGradeCode gradeCode, int tokenAmount, String rewardMonth) {
        GradeJpaEntity grade = requireGrade(gradeCode);
        gradeRewardHistoryJpaRepository.save(GradeRewardHistoryJpaEntity.builder()
                .memberUuid(memberUuid)
                .gradeId(grade.getGradeId())
                .rewardMonth(rewardMonth)
                .tokenAmount(tokenAmount)
                .rewardStatus(GradeRewardStatus.GRANTED)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    public int monthlyTokenAmount(MemberGradeCode gradeCode) {
        GradeJpaEntity grade = requireGrade(gradeCode);
        return gradeBenefitJpaRepository.findByGradeId(grade.getGradeId()).stream()
                .filter(b -> b.getBenefitCode() == GradeBenefitCode.MONTHLY_TOKEN)
                .map(GradeBenefitJpaEntity::getBenefitValue)
                .filter(Objects::nonNull)
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(gradeCode.monthlyTokenAmount());
    }

    @Override
    public List<GradeRewardView> listRewards(String memberUuid) {
        Map<Long, GradeJpaEntity> grades = gradeJpaRepository.findAll().stream()
                .collect(Collectors.toMap(GradeJpaEntity::getGradeId, Function.identity()));
        return gradeRewardHistoryJpaRepository.findByMemberUuidOrderByCreatedAtDesc(memberUuid).stream()
                .map(row -> GradeRewardView.builder()
                        .memberUuid(row.getMemberUuid())
                        .gradeCode(grades.get(row.getGradeId()).getGradeCode())
                        .rewardMonth(row.getRewardMonth())
                        .tokenAmount(row.getTokenAmount())
                        .rewardStatus(row.getRewardStatus().name())
                        .createdAt(row.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<MemberGradeAssignment> listAssignmentsForActiveMembers() {
        Map<Long, GradeJpaEntity> grades = gradeJpaRepository.findAll().stream()
                .collect(Collectors.toMap(GradeJpaEntity::getGradeId, Function.identity()));
        return gradeMemberJpaRepository.findAll().stream()
                .filter(assignment -> memberJpaRepository.findByMemberUuid(assignment.getMemberUuid())
                        .map(m -> m.getStatus() == UserStatus.ACTIVE)
                        .orElse(false))
                .map(assignment -> new MemberGradeAssignment(
                        assignment.getMemberUuid(),
                        MemberGradeCode.fromCode(grades.get(assignment.getGradeId()).getGradeCode())
                ))
                .toList();
    }

    private void ensureInitialized(String memberUuid) {
        if (gradeMemberJpaRepository.existsById(memberUuid) && memberGradeMetricJpaRepository.existsByMemberUuid(memberUuid)) {
            return;
        }
        MemberJpaEntity member = memberJpaRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        initializeMember(member.getMemberId(), member.getMemberUuid());
    }

    private MemberGradeView buildView(GradeMemberJpaEntity assignment) {
        GradeJpaEntity grade = requireGradeEntity(assignment.getGradeId());
        List<MemberGradeView.Metric> metrics = memberGradeMetricJpaRepository.findByMemberUuid(assignment.getMemberUuid())
                .stream()
                .sorted(Comparator.comparing(m -> m.getMetricType().name()))
                .map(m -> MemberGradeView.Metric.builder()
                        .metricType(m.getMetricType().name())
                        .currentValue(m.getCurrentValue())
                        .sourceService(m.getSourceService())
                        .sourceVersion(m.getSourceVersion())
                        .synchronizedAt(m.getSynchronizedAt())
                        .build())
                .toList();
        List<GradeCatalogItem.Benefit> benefits = gradeBenefitJpaRepository.findByGradeId(grade.getGradeId()).stream()
                .sorted(Comparator.comparing(GradeBenefitJpaEntity::getSortOrder))
                .map(this::toBenefit)
                .toList();
        return MemberGradeView.builder()
                .memberUuid(assignment.getMemberUuid())
                .gradeUuid(assignment.getGradeUuid())
                .gradeCode(grade.getGradeCode())
                .gradeName(grade.getGradeName())
                .gradeLevel(grade.getGradeLevel())
                .gradeStatus(assignment.getGradeStatus().name())
                .gradeAssignedAt(assignment.getGradeAssignedAt())
                .lastEvaluatedAt(assignment.getLastEvaluatedAt())
                .metrics(metrics)
                .benefits(benefits)
                .build();
    }

    private List<GradeCatalogItem> toCatalog(List<GradeJpaEntity> grades) {
        if (grades.isEmpty()) {
            return List.of();
        }
        List<Long> ids = grades.stream().map(GradeJpaEntity::getGradeId).toList();
        Map<Long, List<GradeConditionJpaEntity>> conditions = gradeConditionJpaRepository.findByGradeIdIn(ids).stream()
                .collect(Collectors.groupingBy(GradeConditionJpaEntity::getGradeId));
        Map<Long, List<GradeBenefitJpaEntity>> benefits = gradeBenefitJpaRepository.findByGradeIdIn(ids).stream()
                .collect(Collectors.groupingBy(GradeBenefitJpaEntity::getGradeId));

        return grades.stream()
                .map(grade -> GradeCatalogItem.builder()
                        .gradeCode(grade.getGradeCode())
                        .gradeName(grade.getGradeName())
                        .gradeLevel(grade.getGradeLevel())
                        .description(grade.getDescription())
                        .conditions(conditions.getOrDefault(grade.getGradeId(), List.of()).stream()
                                .sorted(Comparator.comparing(GradeConditionJpaEntity::getSortOrder))
                                .map(c -> GradeCatalogItem.Condition.builder()
                                        .metricType(c.getMetricType().name())
                                        .conditionName(c.getConditionName())
                                        .thresholdValue(c.getThresholdValue())
                                        .sortOrder(c.getSortOrder())
                                        .description(c.getDescription())
                                        .build())
                                .toList())
                        .benefits(benefits.getOrDefault(grade.getGradeId(), List.of()).stream()
                                .sorted(Comparator.comparing(GradeBenefitJpaEntity::getSortOrder))
                                .map(this::toBenefit)
                                .toList())
                        .build())
                .toList();
    }

    private GradeCatalogItem.Benefit toBenefit(GradeBenefitJpaEntity b) {
        return GradeCatalogItem.Benefit.builder()
                .benefitCode(b.getBenefitCode().name())
                .benefitName(b.getBenefitName())
                .benefitValue(b.getBenefitValue())
                .description(b.getDescription())
                .sortOrder(b.getSortOrder())
                .build();
    }

    private GradeJpaEntity requireGrade(MemberGradeCode code) {
        return gradeJpaRepository.findByGradeCode(code.name())
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_ERROR));
    }

    private GradeJpaEntity requireGradeEntity(Long gradeId) {
        return gradeJpaRepository.findById(gradeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_ERROR));
    }
}
