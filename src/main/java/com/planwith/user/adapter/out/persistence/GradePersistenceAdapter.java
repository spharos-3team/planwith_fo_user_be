package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.*;
import com.planwith.user.adapter.out.persistence.repository.*;
import com.planwith.user.application.dto.GradeCatalogItem;
import com.planwith.user.application.dto.GradeRewardView;
import com.planwith.user.application.dto.MemberGradeView;
import com.planwith.user.application.port.out.MemberGradePort;
import com.planwith.user.domain.grade.GradeRewardType;
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

    private final GradeJpaRepository gradeJpaRepository;
    private final GradeConditionJpaRepository gradeConditionJpaRepository;
    private final GradeBenefitJpaRepository gradeBenefitJpaRepository;
    private final MemberGradeJpaRepository memberGradeJpaRepository;
    private final MemberGradeMetricJpaRepository memberGradeMetricJpaRepository;
    private final GradeRewardHistoryJpaRepository gradeRewardHistoryJpaRepository;
    private final MemberProfileJpaRepository memberProfileJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public List<GradeCatalogItem> listCatalog() {
        List<GradeJpaEntity> grades = gradeJpaRepository.findAllByOrderBySortOrderAsc();
        return toCatalog(grades);
    }

    @Override
    public Optional<GradeCatalogItem> findCatalogByCode(MemberGradeCode code) {
        return gradeJpaRepository.findByGradeCode(code.name()).map(grade -> toCatalog(List.of(grade)).get(0));
    }

    @Override
    @Transactional
    public void initializeMember(Long memberId, String memberUuid) {
        GradeJpaEntity rookie = requireGrade(MemberGradeCode.ROOKIE);
        LocalDateTime now = LocalDateTime.now();
        if (!memberGradeJpaRepository.existsById(memberId)) {
            memberGradeJpaRepository.save(MemberGradeJpaEntity.builder()
                    .memberId(memberId)
                    .memberUuid(memberUuid)
                    .gradeId(rookie.getGradeId())
                    .gradedAt(now)
                    .build());
        }
        if (!memberGradeMetricJpaRepository.existsById(memberId)) {
            memberGradeMetricJpaRepository.save(MemberGradeMetricJpaEntity.builder()
                    .memberId(memberId)
                    .memberUuid(memberUuid)
                    .storyCount(0)
                    .followerCount(0)
                    .likeCount(0)
                    .metricsUpdatedAt(now)
                    .build());
        }
        mirrorProfileGrade(memberId, MemberGradeCode.ROOKIE);
    }

    @Override
    @Transactional
    public MemberGradeView getMemberGrade(Long memberId) {
        ensureInitialized(memberId);
        return buildView(memberGradeJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    @Transactional
    public MemberGradeView getMemberGradeByUuid(String memberUuid) {
        MemberJpaEntity member = memberJpaRepository.findByMemberUuid(memberUuid)
                .filter(m -> m.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        ensureInitialized(member.getMemberId());
        return buildView(memberGradeJpaRepository.findById(member.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    @Transactional
    public MemberGradeCode getCurrentGradeCode(Long memberId) {
        ensureInitialized(memberId);
        Long gradeId = memberGradeJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
                .getGradeId();
        return MemberGradeCode.fromCode(requireGradeEntity(gradeId).getGradeCode());
    }

    @Override
    @Transactional
    public void saveMetrics(Long memberId, String memberUuid, long storyCount, long followerCount, long likeCount) {
        ensureInitialized(memberId);
        MemberGradeMetricJpaEntity metric = memberGradeMetricJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        metric.updateCounts(storyCount, followerCount, likeCount, LocalDateTime.now());
        memberGradeMetricJpaRepository.save(metric);
    }

    @Override
    @Transactional
    public void updateFollowerCount(Long memberId, String memberUuid, long followerCount) {
        ensureInitialized(memberId);
        MemberGradeMetricJpaEntity metric = memberGradeMetricJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        metric.updateFollowerCount(followerCount, LocalDateTime.now());
        memberGradeMetricJpaRepository.save(metric);
    }

    @Override
    @Transactional
    public void updateMemberGrade(Long memberId, String memberUuid, MemberGradeCode gradeCode) {
        ensureInitialized(memberId);
        GradeJpaEntity grade = requireGrade(gradeCode);
        MemberGradeJpaEntity assignment = memberGradeJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        assignment.changeGrade(grade.getGradeId(), LocalDateTime.now());
        memberGradeJpaRepository.save(assignment);
        mirrorProfileGrade(memberId, gradeCode);
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
    public boolean rewardExists(Long memberId, String periodYm) {
        return gradeRewardHistoryJpaRepository.existsByMemberIdAndRewardTypeAndPeriodYm(
                memberId, GradeRewardType.MONTHLY_TOKEN, periodYm);
    }

    @Override
    @Transactional
    public void saveMonthlyReward(Long memberId, MemberGradeCode gradeCode, int amount, String periodYm) {
        GradeJpaEntity grade = requireGrade(gradeCode);
        gradeRewardHistoryJpaRepository.save(GradeRewardHistoryJpaEntity.builder()
                .rewardUuid(UUID.randomUUID().toString())
                .memberId(memberId)
                .gradeId(grade.getGradeId())
                .rewardType(GradeRewardType.MONTHLY_TOKEN)
                .amount(amount)
                .periodYm(periodYm)
                .grantedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public List<GradeRewardView> listRewards(Long memberId) {
        Map<Long, GradeJpaEntity> grades = gradeJpaRepository.findAll().stream()
                .collect(Collectors.toMap(GradeJpaEntity::getGradeId, Function.identity()));
        return gradeRewardHistoryJpaRepository.findByMemberIdOrderByGrantedAtDesc(memberId).stream()
                .map(row -> GradeRewardView.builder()
                        .rewardUuid(row.getRewardUuid())
                        .gradeCode(grades.get(row.getGradeId()).getGradeCode())
                        .rewardType(row.getRewardType().name())
                        .amount(row.getAmount())
                        .periodYm(row.getPeriodYm())
                        .grantedAt(row.getGrantedAt())
                        .build())
                .toList();
    }

    @Override
    public List<MemberGradeAssignment> listAssignmentsForActiveMembers() {
        Map<Long, GradeJpaEntity> grades = gradeJpaRepository.findAll().stream()
                .collect(Collectors.toMap(GradeJpaEntity::getGradeId, Function.identity()));
        return memberGradeJpaRepository.findAll().stream()
                .filter(assignment -> memberJpaRepository.findById(assignment.getMemberId())
                        .map(m -> m.getStatus() == UserStatus.ACTIVE)
                        .orElse(false))
                .map(assignment -> new MemberGradeAssignment(
                        assignment.getMemberId(),
                        assignment.getMemberUuid(),
                        MemberGradeCode.fromCode(grades.get(assignment.getGradeId()).getGradeCode())
                ))
                .toList();
    }

    private void ensureInitialized(Long memberId) {
        if (memberGradeJpaRepository.existsById(memberId) && memberGradeMetricJpaRepository.existsById(memberId)) {
            return;
        }
        MemberJpaEntity member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        initializeMember(member.getMemberId(), member.getMemberUuid());
    }

    private MemberGradeView buildView(MemberGradeJpaEntity assignment) {
        GradeJpaEntity grade = requireGradeEntity(assignment.getGradeId());
        MemberGradeMetricJpaEntity metric = memberGradeMetricJpaRepository.findById(assignment.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<GradeCatalogItem.Benefit> benefits = gradeBenefitJpaRepository.findByGradeId(grade.getGradeId()).stream()
                .map(b -> GradeCatalogItem.Benefit.builder()
                        .benefitCode(b.getBenefitCode().name())
                        .description(b.getDescription())
                        .build())
                .toList();
        return MemberGradeView.builder()
                .memberUuid(assignment.getMemberUuid())
                .gradeCode(grade.getGradeCode())
                .nameKo(grade.getNameKo())
                .sortOrder(grade.getSortOrder())
                .monthlyTokenAmount(grade.getMonthlyTokenAmount())
                .gradedAt(assignment.getGradedAt())
                .metrics(MemberGradeView.Metrics.builder()
                        .storyCount(metric.getStoryCount())
                        .followerCount(metric.getFollowerCount())
                        .likeCount(metric.getLikeCount())
                        .metricsUpdatedAt(metric.getMetricsUpdatedAt())
                        .build())
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
                        .nameKo(grade.getNameKo())
                        .sortOrder(grade.getSortOrder())
                        .monthlyTokenAmount(grade.getMonthlyTokenAmount())
                        .conditions(conditions.getOrDefault(grade.getGradeId(), List.of()).stream()
                                .map(c -> GradeCatalogItem.Condition.builder()
                                        .metricType(c.getMetricType().name())
                                        .threshold(c.getThresholdValue())
                                        .build())
                                .toList())
                        .benefits(benefits.getOrDefault(grade.getGradeId(), List.of()).stream()
                                .map(b -> GradeCatalogItem.Benefit.builder()
                                        .benefitCode(b.getBenefitCode().name())
                                        .description(b.getDescription())
                                        .build())
                                .toList())
                        .build())
                .toList();
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
