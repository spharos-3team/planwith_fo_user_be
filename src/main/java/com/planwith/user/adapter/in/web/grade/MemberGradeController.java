package com.planwith.user.adapter.in.web.grade;

import com.planwith.user.adapter.in.gateway.AuthenticatedUserContext;
import com.planwith.user.adapter.in.gateway.GatewayAuthenticationContextResolver;
import com.planwith.user.adapter.in.web.grade.dto.GradeRewardResponse;
import com.planwith.user.adapter.in.web.grade.dto.MemberGradeResponse;
import com.planwith.user.application.port.in.GetMemberGradeUseCase;
import com.planwith.user.application.port.in.ListGradeRewardsUseCase;
import com.planwith.user.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberGradeController {

    private final GetMemberGradeUseCase getMemberGradeUseCase;
    private final ListGradeRewardsUseCase listGradeRewardsUseCase;

    @GetMapping("/me/grade")
    public ResponseEntity<ApiResponse<MemberGradeResponse>> myGrade(AuthenticatedUserContext userContext) {
        AuthenticatedUserContext auth = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        var view = getMemberGradeUseCase.getMyGrade(Long.valueOf(auth.userId()));
        return ResponseEntity.ok(ApiResponse.success(MemberGradeResponse.from(view)));
    }

    @GetMapping("/me/grade/rewards")
    public ResponseEntity<ApiResponse<List<GradeRewardResponse>>> myRewards(AuthenticatedUserContext userContext) {
        AuthenticatedUserContext auth = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        List<GradeRewardResponse> data = listGradeRewardsUseCase.listMyRewards(Long.valueOf(auth.userId())).stream()
                .map(GradeRewardResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{memberUuid}/grade")
    public ResponseEntity<ApiResponse<MemberGradeResponse>> memberGrade(@PathVariable String memberUuid) {
        var view = getMemberGradeUseCase.getByMemberUuid(memberUuid);
        return ResponseEntity.ok(ApiResponse.success(MemberGradeResponse.from(view)));
    }
}
