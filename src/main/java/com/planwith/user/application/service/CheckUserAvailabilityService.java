package com.planwith.user.application.service;

import com.planwith.user.application.port.in.CheckEmailDuplicationUseCase;
import com.planwith.user.application.port.in.CheckNicknameDuplicationUseCase;
import com.planwith.user.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckUserAvailabilityService implements CheckEmailDuplicationUseCase, CheckNicknameDuplicationUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public boolean isEmailDuplicated(String email) {
        return userRepositoryPort.existsActiveByEmail(email);
    }

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return userRepositoryPort.existsActiveByNickname(nickname);
    }
}
