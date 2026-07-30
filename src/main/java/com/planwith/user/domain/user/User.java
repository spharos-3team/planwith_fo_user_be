package com.planwith.user.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {

    private Long id;
    private Long gradeId;
    private Long followId;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private String introduction;
    private LoginType loginType;
    private String providerId;
    private UserStatus status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PUBLIC)
    public User(Long id, Long gradeId, Long followId, String email, String password, String nickname,
                String profileImage, String introduction, LoginType loginType, String providerId,
                UserStatus status, String role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.gradeId = gradeId;
        this.followId = followId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.introduction = introduction;
        this.loginType = loginType;
        this.providerId = providerId;
        this.status = status;
        this.role = (role == null) ? "USER" : role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User createLocal(Long gradeId, String email, String encodedPassword, String nickname,
                                   String profileImage, String introduction) {
        return User.builder()
                .gradeId(gradeId)
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

    public static User createSocial(Long gradeId, String email, String nickname,
                                    LoginType loginType, String providerId) {
        return User.builder()
                .gradeId(gradeId)
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

    /**
     * Soft withdraw with anonymization so unique email/nickname constraints are released immediately.
     */
    public void withdraw() {
        this.status = UserStatus.DELETED;
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
