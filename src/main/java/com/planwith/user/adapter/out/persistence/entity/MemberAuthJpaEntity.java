package com.planwith.user.adapter.out.persistence.entity;

import com.planwith.user.domain.user.LoginType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_auths")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAuthJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(name = "social_id", length = 100)
    private String socialId;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MemberAuthJpaEntity(Long authId, Long memberId, LoginType loginType, String email, String password,
                               String socialId, LocalDateTime lastLoginAt,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.authId = authId;
        this.memberId = memberId;
        this.loginType = loginType;
        this.email = email;
        this.password = password;
        this.socialId = socialId;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void apply(String email, String password, String socialId, LocalDateTime lastLoginAt) {
        this.email = email;
        this.password = password;
        this.socialId = socialId;
        this.lastLoginAt = lastLoginAt;
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
