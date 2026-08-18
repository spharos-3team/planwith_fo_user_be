package com.planwith.user.adapter.in.web.terms;

import com.planwith.user.adapter.in.web.terms.dto.TermsResponse;
import com.planwith.user.application.port.in.GetActiveTermsUseCase;
import com.planwith.user.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final GetActiveTermsUseCase getActiveTermsUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TermsResponse>>> getTerms() {
        List<TermsResponse> response = getActiveTermsUseCase.getActiveTerms().stream()
                .map(TermsResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
