package com.planwith.user.adapter.in.web.grade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GradeEvaluateRequest {

    @NotBlank
    private String memberUuid;

    @Min(0)
    private long storyCount;

    @Min(0)
    private long likeCount;
}
