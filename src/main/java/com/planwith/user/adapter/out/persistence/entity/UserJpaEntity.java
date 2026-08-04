package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "follow_id")
    private Long followId;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(length = 100)
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserJpaEntity(Long id, Long gradeId, Long followId, String email, String password, String nickname,
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

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
