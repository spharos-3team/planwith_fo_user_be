package com.planwith.user.adapter.in.web.grade;

import com.planwith.user.adapter.in.web.grade.dto.GradeCatalogResponse;
import com.planwith.user.application.port.in.ListGradesUseCase;
import com.planwith.user.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final ListGradesUseCase listGradesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GradeCatalogResponse>>> list() {
        List<GradeCatalogResponse> data = listGradesUseCase.listGrades().stream()
                .map(GradeCatalogResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
