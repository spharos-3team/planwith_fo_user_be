package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.EmailVerificationJpaEntity;
import com.planwith.user.adapter.out.persistence.repository.EmailVerificationJpaRepository;
import com.planwith.user.application.port.out.EmailVerificationPort;
import com.planwith.user.domain.email.EmailVerification;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationPersistenceAdapter implements EmailVerificationPort {

    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int VERIFIED_GRACE_MINUTES = 30;

    private final EmailVerificationJpaRepository emailVerificationJpaRepository;

    @Override
    @Transactional
    public void saveNewCode(String email, String code) {
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedCode = code == null ? "" : code.trim();
        EmailVerification domain = EmailVerification.createPending(
                normalizedEmail, normalizedCode, LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));

        emailVerificationJpaRepository.save(EmailVerificationJpaEntity.builder()
                .email(domain.getEmail())
                .code(domain.getCode())
                .expiresAt(domain.getExpiresAt())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerification> findLatestByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim();
        return emailVerificationJpaRepository.findTopByEmailOrderByCreatedAtDesc(normalizedEmail)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void markVerified(Long id) {
        EmailVerificationJpaEntity entity = emailVerificationJpaRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));
        entity.markVerified();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(VERIFIED_GRACE_MINUTES);
        return findLatestByEmail(email)
                .map(v -> v.isVerifiedWithin(since))
                .orElse(false);
    }

    private EmailVerification toDomain(EmailVerificationJpaEntity entity) {
        return EmailVerification.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .code(entity.getCode())
                .verified(entity.isVerified())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
