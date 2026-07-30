package com.planwith.user.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "인증 요청 내역이 없습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "리프레시 토큰 재사용이 감지되어 세션이 폐기되었습니다."),
    GATEWAY_TRUST_FAILED(HttpStatus.UNAUTHORIZED, "Gateway 내부 토큰이 유효하지 않습니다."),
    GATEWAY_TRUST_MISCONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "Gateway Trust 설정이 올바르지 않습니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    SSE_TICKET_INVALID(HttpStatus.UNAUTHORIZED, "SSE 티켓이 유효하지 않습니다."),
    ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "이미 탈퇴한 계정입니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해주세요."),
    PROFANITY_DETECTED(HttpStatus.BAD_REQUEST, "닉네임 또는 소개글에 사용할 수 없는 표현이 포함되어 있습니다."),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "이미지는 jpg, jpeg, png, webp 형식만 등록할 수 있습니다."),
    IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 용량은 5MB를 초과할 수 없습니다."),
    INVALID_IMAGE_DIMENSION(HttpStatus.BAD_REQUEST, "프로필 사진은 400x400 픽셀 정사각형이어야 합니다."),
    SOCIAL_ACCOUNT_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 소셜 계정입니다."),
    INAPPROPRIATE_IMAGE(HttpStatus.BAD_REQUEST, "부적절한 콘텐츠가 감지되어 등록할 수 없는 이미지입니다."),
    IMAGE_MODERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "이미지 검열 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

    private final HttpStatus status;
    private final String message;

    /** String code used in ApiResponse.error.code (enum name). */
    public String getCode() {
        return name();
    }
}
