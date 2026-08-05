package com.planwith.user.application.port.out;

import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findActiveById(Long id);

    Optional<User> findActiveByMemberUuid(String memberUuid);

    Optional<User> findActiveByEmail(String email);

    boolean existsActiveByEmail(String email);

    boolean existsActiveByNickname(String nickname);

    boolean existsActiveByNicknameExcludingMemberId(String nickname, Long memberId);

    Optional<User> findActiveByLoginTypeAndProviderId(LoginType loginType, String providerId);
}
