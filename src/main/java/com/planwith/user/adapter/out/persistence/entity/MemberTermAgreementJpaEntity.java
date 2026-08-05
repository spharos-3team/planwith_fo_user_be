package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_term_agreements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTermAgreementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Column(nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    @Builder
    public MemberTermAgreementJpaEntity(Long termId, String memberUuid, boolean agreed) {
        this.termId = termId;
        this.memberUuid = memberUuid;
        this.agreed = agreed;
        this.agreedAt = LocalDateTime.now();
    }
}
