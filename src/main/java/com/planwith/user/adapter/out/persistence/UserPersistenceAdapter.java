package com.planwith.user.adapter.out.persistence;

import com.planwith.user.adapter.out.persistence.entity.MemberAuthJpaEntity;
import com.planwith.user.adapter.out.persistence.entity.MemberJpaEntity;
import com.planwith.user.adapter.out.persistence.entity.MemberProfileJpaEntity;
import com.planwith.user.adapter.out.persistence.mapper.UserMapper;
import com.planwith.user.adapter.out.persistence.repository.MemberAuthJpaRepository;
import com.planwith.user.adapter.out.persistence.repository.MemberJpaRepository;
import com.planwith.user.adapter.out.persistence.repository.MemberProfileJpaRepository;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberAuthJpaRepository memberAuthJpaRepository;
    private final MemberProfileJpaRepository memberProfileJpaRepository;

    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            MemberJpaEntity member = memberJpaRepository.save(UserMapper.toNewMemberEntity(user));
            MemberAuthJpaEntity auth = memberAuthJpaRepository.save(
                    UserMapper.toNewAuthEntity(member.getMemberId(), user));
            MemberProfileJpaEntity profile = memberProfileJpaRepository.save(
                    UserMapper.toNewProfileEntity(member.getMemberId(), user));
            return UserMapper.toDomain(member, auth, profile);
        }

        MemberJpaEntity existingOrNew = memberJpaRepository.findById(user.getId())
                .orElseGet(() -> UserMapper.toNewMemberEntity(user));
        existingOrNew.apply(user.getStatus(), user.getDeletedAt());
        MemberJpaEntity member = memberJpaRepository.save(existingOrNew);

        MemberAuthJpaEntity auth = memberAuthJpaRepository.findByMemberId(member.getMemberId())
                .orElseGet(() -> UserMapper.toNewAuthEntity(member.getMemberId(), user));
        auth.apply(user.getEmail(), user.getPassword(), user.getProviderId(), user.getLastLoginAt());
        auth = memberAuthJpaRepository.save(auth);

        MemberProfileJpaEntity profile = memberProfileJpaRepository.findById(member.getMemberId())
                .orElseGet(() -> UserMapper.toNewProfileEntity(member.getMemberId(), user));
        profile.apply(user.getNickname(), user.getProfileImage(), user.getIntroduction(), user.getGrade());
        profile = memberProfileJpaRepository.save(profile);

        return UserMapper.toDomain(member, auth, profile);
    }

    @Override
    public Optional<User> findById(Long id) {
        return memberJpaRepository.findById(id).flatMap(this::assemble);
    }

    @Override
    public Optional<User> findActiveById(Long id) {
        return loadActiveMember(id).flatMap(this::assemble);
    }

    @Override
    public Optional<User> findActiveByMemberUuid(String memberUuid) {
        return memberJpaRepository.findByMemberUuid(memberUuid)
                .filter(member -> member.getStatus() == UserStatus.ACTIVE)
                .flatMap(this::assemble);
    }

    @Override
    public Optional<User> findActiveByEmail(String email) {
        return memberAuthJpaRepository.findByLoginTypeAndEmail(LoginType.LOCAL, email)
                .flatMap(auth -> loadActiveMember(auth.getMemberId())
                        .flatMap(member -> assemble(member, auth)));
    }

    @Override
    public boolean existsActiveByEmail(String email) {
        return memberAuthJpaRepository.findByLoginTypeAndEmail(LoginType.LOCAL, email)
                .flatMap(auth -> loadActiveMember(auth.getMemberId()))
                .isPresent();
    }

    @Override
    public boolean existsActiveByNickname(String nickname) {
        return memberProfileJpaRepository.existsActiveByNickname(nickname, UserStatus.DELETED);
    }

    @Override
    public boolean existsActiveByNicknameExcludingMemberId(String nickname, Long memberId) {
        return memberProfileJpaRepository.existsActiveByNicknameExcludingMemberId(
                nickname, memberId, UserStatus.DELETED);
    }

    @Override
    public Optional<User> findActiveByLoginTypeAndProviderId(LoginType loginType, String providerId) {
        return memberAuthJpaRepository.findByLoginTypeAndSocialId(loginType, providerId)
                .flatMap(auth -> loadActiveMember(auth.getMemberId())
                        .flatMap(member -> assemble(member, auth)));
    }

    private Optional<MemberJpaEntity> loadActiveMember(Long memberId) {
        return memberJpaRepository.findByMemberIdAndStatus(memberId, UserStatus.ACTIVE);
    }

    private Optional<User> assemble(MemberJpaEntity member) {
        Optional<MemberAuthJpaEntity> auth = memberAuthJpaRepository.findByMemberId(member.getMemberId());
        Optional<MemberProfileJpaEntity> profile = memberProfileJpaRepository.findById(member.getMemberId());
        if (auth.isEmpty() || profile.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(UserMapper.toDomain(member, auth.get(), profile.get()));
    }

    private Optional<User> assemble(MemberJpaEntity member, MemberAuthJpaEntity auth) {
        return memberProfileJpaRepository.findById(member.getMemberId())
                .map(profile -> UserMapper.toDomain(member, auth, profile));
    }
}
