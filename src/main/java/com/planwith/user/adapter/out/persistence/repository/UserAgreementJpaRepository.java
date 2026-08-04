package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.UserAgreementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAgreementJpaRepository extends JpaRepository<UserAgreementJpaEntity, Long> {
}
