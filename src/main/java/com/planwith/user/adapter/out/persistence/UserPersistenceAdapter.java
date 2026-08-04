package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.UserJpaEntity;
import com.planwith.user.adapter.out.persistence.mapper.UserMapper;
import com.planwith.user.adapter.out.persistence.repository.UserJpaRepository;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.save(UserMapper.toEntity(user));
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findActiveByEmail(String email) {
        return userJpaRepository.findByEmailAndStatusNot(email, UserStatus.DELETED)
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsActiveByEmail(String email) {
        return userJpaRepository.existsByEmailAndStatusNot(email, UserStatus.DELETED);
    }

    @Override
    public boolean existsActiveByNickname(String nickname) {
        return userJpaRepository.existsByNicknameAndStatusNot(nickname, UserStatus.DELETED);
    }

    @Override
    public Optional<User> findActiveByLoginTypeAndProviderId(LoginType loginType, String providerId) {
        return userJpaRepository.findByLoginTypeAndProviderIdAndStatusNot(loginType, providerId, UserStatus.DELETED)
                .map(UserMapper::toDomain);
    }
}
