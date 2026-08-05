package com.planwith.user.adapter.in.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,20}$",
            message = "비밀번호는 영문, 특수문자를 포함해 8~20자로 입력해주세요."
    )
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자로 입력해주세요.")
    private String nickname;

    private String profileImage;

    @Size(max = 20, message = "소개는 20자 이내로 작성해주세요.")
    private String introduction;

    private List<Long> agreedTermIds;
}
