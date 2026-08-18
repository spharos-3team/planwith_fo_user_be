package com.planwith.user.application.port.out;

public interface MailSenderPort {

    void sendVerificationCode(String email, String code);
}
