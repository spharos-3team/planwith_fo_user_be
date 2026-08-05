package com.planwith.user.application.port.in;

public interface CheckNicknameDuplicationUseCase {
    boolean isNicknameDuplicated(String nickname);
}
