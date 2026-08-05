package com.planwith.user.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class User {

    public static final String DEFAULT_GRADE = "일반회원";

    private Long id;
    private String memberUuid;
    private String grade;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private String introduction;
    private LoginType loginType;
    private String providerId;
    private UserStatus status;
    private String role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PUBLIC)
    public User(Long id, String memberUuid, String grade, String email, String password, String nickname,
                String profileImage, String introduction, LoginType loginType, String providerId,
                UserStatus status, String role, LocalDateTime lastLoginAt,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.memberUuid = memberUuid;
        this.grade = (grade == null || grade.isBlank()) ? DEFAULT_GRADE : grade;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.introduction = introduction;
        this.loginType = loginType;
        this.providerId = providerId;
        this.status = status;
        this.role = (role == null) ? "USER" : role;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static User createLocal(String grade, String email, String encodedPassword, String nickname,
                                   String profileImage, String introduction) {
        return User.builder()
                .memberUuid(UUID.randomUUID().toString())
                .grade(grade)
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(profileImage)
                .introduction(introduction)
                .loginType(LoginType.LOCAL)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
    }

    public static User createSocial(String grade, String email, String nickname,
                                    LoginType loginType, String providerId) {
        return User.builder()
                .memberUuid(UUID.randomUUID().toString())
                .grade(grade)
                .email(email)
                .nickname(nickname)
                .loginType(loginType)
                .providerId(providerId)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void recordLastLogin(LocalDateTime at) {
        this.lastLoginAt = at;
    }

    public void updateProfile(String nickname, String profileImage, String introduction) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname.trim();
        }
        if (profileImage != null) {
            this.profileImage = profileImage.isBlank() ? null : profileImage.trim();
        }
        if (introduction != null) {
            this.introduction = introduction.isBlank() ? null : introduction.trim();
        }
    }

    /**
     * Soft withdraw with anonymization so unique email/nickname constraints are released immediately.
     * Sets deleted_at and status DELETED; auth/profile fields are cleared for persistence.
     */
    public void withdraw() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.email = "deleted_" + this.id + "_" + System.currentTimeMillis() + "@withdrawn.local";
        this.nickname = "탈퇴회원_" + this.id;
        this.password = null;
        this.profileImage = null;
        this.introduction = null;
        this.providerId = null;
    }

    public boolean isDeleted() {
        return this.status == UserStatus.DELETED;
    }

    public boolean isLocalAccount() {
        return this.loginType == LoginType.LOCAL;
    }
}
