package com.planwith.user.adapter.in.web.member.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 10, message = "닉네임은 2~10자로 입력해주세요.")
    private String nickname;

    @Size(max = 1000, message = "프로필 이미지 URL이 너무 깁니다.")
    private String profileImage;

    @Size(max = 20, message = "소개는 20자 이내로 작성해주세요.")
    private String profileIntro;
}
