package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor
public class EmailVerificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public EmailVerificationJpaEntity(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.verified = false;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public void markVerified() {
        this.verified = true;
    }
}
