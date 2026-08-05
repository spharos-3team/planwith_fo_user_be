package com.planwith.user.adapter.in.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequest {

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.")
    private String code;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,20}$",
            message = "비밀번호는 영문, 특수문자를 포함해 8~20자로 입력해주세요."
    )
    private String newPassword;
}
