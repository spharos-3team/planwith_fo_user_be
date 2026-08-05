package com.planwith.user.adapter.in.web.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawRequest {

    /**
     * Required for LOCAL accounts. Social accounts may omit or send blank.
     */
    private String password;
}
