package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.UserAgreementJpaEntity;
import com.planwith.user.adapter.out.persistence.repository.UserAgreementJpaRepository;
import com.planwith.user.application.port.out.UserAgreementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAgreementPersistenceAdapter implements UserAgreementPort {

    private final UserAgreementJpaRepository userAgreementJpaRepository;

    @Override
    public void saveAgreements(Long userId, List<Long> termsIds) {
        if (termsIds == null || termsIds.isEmpty()) {
            return;
        }
        List<UserAgreementJpaEntity> agreements = termsIds.stream()
                .map(termsId -> UserAgreementJpaEntity.builder().userId(userId).termsId(termsId).build())
                .toList();
        userAgreementJpaRepository.saveAll(agreements);
    }
}
