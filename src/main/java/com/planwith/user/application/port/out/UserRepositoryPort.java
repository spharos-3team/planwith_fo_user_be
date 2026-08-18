package com.planwith.user.application.port.out;

import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findActiveById(Long id);

    Optional<User> findActiveByMemberUuid(String memberUuid);

    Optional<User> findActiveByNickname(String nickname);

    Optional<User> findActiveByEmail(String email);

    /** 상태와 무관하게 이메일로 조회 (정지 계정 판별용) */
    Optional<User> findByEmail(String email);

    /** 상태와 무관하게 소셜 계정 조회 (정지 계정 판별용) */
    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);

    boolean existsActiveByEmail(String email);

    boolean existsActiveByNickname(String nickname);

    boolean existsActiveByNicknameExcludingMemberId(String nickname, Long memberId);

    Optional<User> findActiveByLoginTypeAndProviderId(LoginType loginType, String providerId);
}
