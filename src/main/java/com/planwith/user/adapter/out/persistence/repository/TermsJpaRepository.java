package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.TermsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermsJpaRepository extends JpaRepository<TermsJpaEntity, Long> {

    List<TermsJpaEntity> findAllByActiveTrueOrderByDisplayOrderAsc();

    List<TermsJpaEntity> findAllByActiveTrueAndRequiredTrue();
}
