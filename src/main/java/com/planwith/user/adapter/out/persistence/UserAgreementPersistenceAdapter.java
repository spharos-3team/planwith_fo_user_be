package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.MemberTermAgreementJpaEntity;
import com.planwith.user.adapter.out.persistence.repository.MemberTermAgreementJpaRepository;
import com.planwith.user.application.port.out.UserAgreementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAgreementPersistenceAdapter implements UserAgreementPort {

    private final MemberTermAgreementJpaRepository memberTermAgreementJpaRepository;

    @Override
    @Transactional
    public void saveAgreements(String memberUuid, List<Long> termsIds) {
        if (memberUuid == null || memberUuid.isBlank() || termsIds == null || termsIds.isEmpty()) {
            return;
        }
        List<MemberTermAgreementJpaEntity> agreements = termsIds.stream()
                .map(termId -> MemberTermAgreementJpaEntity.builder()
                        .termId(termId)
                        .memberUuid(memberUuid)
                        .agreed(true)
                        .build())
                .toList();
        memberTermAgreementJpaRepository.saveAll(agreements);
    }
}
