package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.TermsJpaEntity;
import com.planwith.user.adapter.out.persistence.repository.TermsJpaRepository;
import com.planwith.user.application.port.out.TermsPort;
import com.planwith.user.domain.terms.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TermsPersistenceAdapter implements TermsPort {

    private final TermsJpaRepository termsJpaRepository;

    @Override
    public List<Terms> findAllActive() {
        return termsJpaRepository.findAllByActiveTrueOrderByIdAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Long> findRequiredActiveIds() {
        return termsJpaRepository.findAllByActiveTrueAndTermType(TermsJpaEntity.TERM_TYPE_REQUIRED).stream()
                .map(TermsJpaEntity::getId)
                .toList();
    }

    private Terms toDomain(TermsJpaEntity entity) {
        return Terms.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .contentUrl(entity.getContent())
                .required(entity.isRequired())
                .displayOrder(entity.getId() != null ? entity.getId().intValue() : 0)
                .active(entity.isActive())
                .build();
    }
}
