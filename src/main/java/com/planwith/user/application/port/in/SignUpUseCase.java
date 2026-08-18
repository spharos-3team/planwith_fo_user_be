package com.planwith.user.application.port.in;

import java.util.List;

public interface SignUpUseCase {

    void signUp(String email, String password, String nickname, String profileImage,
                String introduction, List<Long> agreedTermIds);
}
