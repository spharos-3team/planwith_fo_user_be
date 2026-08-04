package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.UserJpaEntity;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmailAndStatusNot(String email, UserStatus status);

    boolean existsByEmailAndStatusNot(String email, UserStatus status);

    boolean existsByNicknameAndStatusNot(String nickname, UserStatus status);

    Optional<UserJpaEntity> findByLoginTypeAndProviderIdAndStatusNot(
            LoginType loginType, String providerId, UserStatus status);
}
