package com.planwith.user.adapter.in.web.member;

import com.planwith.user.adapter.in.gateway.AuthenticatedUserContext;
import com.planwith.user.adapter.in.gateway.GatewayAuthenticationContextResolver;
import com.planwith.user.adapter.in.web.member.dto.MemberProfileResponse;
import com.planwith.user.adapter.in.web.member.dto.UpdateProfileRequest;
import com.planwith.user.application.port.in.FollowMemberUseCase;
import com.planwith.user.application.port.in.GetMemberProfileUseCase;
import com.planwith.user.application.port.in.GetMyProfileUseCase;
import com.planwith.user.application.port.in.ListFollowUseCase;
import com.planwith.user.application.port.in.UpdateMyProfileUseCase;
import com.planwith.user.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final FollowMemberUseCase followMemberUseCase;
    private final ListFollowUseCase listFollowUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(AuthenticatedUserContext userContext) {
        AuthenticatedUserContext auth = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        var info = getMyProfileUseCase.getMyProfile(Long.valueOf(auth.userId()));
        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(info)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMyProfile(
            AuthenticatedUserContext userContext,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        AuthenticatedUserContext auth = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        var info = updateMyProfileUseCase.updateMyProfile(
                Long.valueOf(auth.userId()),
                request.getNickname(),
                request.getProfileImage(),
                request.getProfileIntro()
        );
        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(info)));
    }

    @GetMapping("/{memberUuid}")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberProfile(
            @PathVariable String memberUuid,
            AuthenticatedUserContext userContext
    ) {
        Long viewerId = userContext.isAuthenticated() ? Long.valueOf(userContext.userId()) : null;
        var info = getMemberProfileUseCase.getByMemberUuid(memberUuid, viewerId);
        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(info)));
    }

    @PostMapping("/{memberUuid}/follow")
    public ResponseEntity<ApiResponse<Void>> follow(
            @PathVariable String memberUuid,
            AuthenticatedUserContext userContext
    ) {
        AuthenticatedUserContext auth = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        followMemberUseCase.follow(Long.valueOf(auth.userId()), memberUuid);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{memberUuid}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable String memberUuid,
            AuthenticatedUserContext userContext
    ) {
        AuthenticatedUserContext auth = GatewayAuthenticationContextResolver.requireAuthenticated(userContext);
        followMemberUseCase.unfollow(Long.valueOf(auth.userId()), memberUuid);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{memberUuid}/followers")
    public ResponseEntity<ApiResponse<List<MemberProfileResponse>>> followers(@PathVariable String memberUuid) {
        List<MemberProfileResponse> list = listFollowUseCase.listFollowers(memberUuid).stream()
                .map(MemberProfileResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{memberUuid}/following")
    public ResponseEntity<ApiResponse<List<MemberProfileResponse>>> following(@PathVariable String memberUuid) {
        List<MemberProfileResponse> list = listFollowUseCase.listFollowing(memberUuid).stream()
                .map(MemberProfileResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
