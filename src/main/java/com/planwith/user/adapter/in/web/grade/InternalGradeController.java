package com.planwith.user.adapter.in.web.grade;

import com.planwith.user.adapter.in.web.grade.dto.GradeEvaluateRequest;
import com.planwith.user.adapter.in.web.grade.dto.GradeEvaluateResponse;
import com.planwith.user.adapter.in.web.grade.dto.MonthlyRewardRequest;
import com.planwith.user.application.port.in.EvaluateMemberGradeUseCase;
import com.planwith.user.application.port.in.GrantMonthlyGradeRewardsUseCase;
import com.planwith.user.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal/grades")
@RequiredArgsConstructor
public class InternalGradeController {

    private final EvaluateMemberGradeUseCase evaluateMemberGradeUseCase;
    private final GrantMonthlyGradeRewardsUseCase grantMonthlyGradeRewardsUseCase;

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<GradeEvaluateResponse>> evaluate(
            @Valid @RequestBody GradeEvaluateRequest request
    ) {
        var result = evaluateMemberGradeUseCase.evaluate(
                request.getMemberUuid(),
                request.getStoryCount(),
                request.getLikeCount()
        );
        return ResponseEntity.ok(ApiResponse.success(GradeEvaluateResponse.from(result)));
    }

    @PostMapping("/rewards/monthly")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> grantMonthly(
            @RequestBody(required = false) MonthlyRewardRequest request
    ) {
        String periodYm = request != null ? request.getPeriodYm() : null;
        int created = grantMonthlyGradeRewardsUseCase.grantForPeriod(periodYm);
        return ResponseEntity.ok(ApiResponse.success(Map.of("createdCount", created)));
    }
}
