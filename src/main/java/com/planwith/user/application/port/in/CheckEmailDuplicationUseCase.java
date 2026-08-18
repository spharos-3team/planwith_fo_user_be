package com.planwith.user.application.port.in;

public interface CheckEmailDuplicationUseCase {
    boolean isEmailDuplicated(String email);
}
