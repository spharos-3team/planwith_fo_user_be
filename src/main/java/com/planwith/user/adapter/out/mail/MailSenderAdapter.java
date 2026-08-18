package com.planwith.user.adapter.out.mail;

import com.planwith.user.application.port.out.MailSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailSenderAdapter implements MailSenderPort {

    private static final int CODE_EXPIRE_MINUTES = 5;

    private final JavaMailSender mailSender;
    private final boolean mockMode;

    public MailSenderAdapter(
            JavaMailSender mailSender,
            @Value("${email.verification.mock-mode:false}") boolean mockMode) {
        this.mailSender = mailSender;
        this.mockMode = mockMode;
    }

    @Override
    public void sendVerificationCode(String email, String code) {
        if (mockMode) {
            // Do not log the verification code — inspect DB (email_verifications) for local testing.
            log.info("[MOCK MAIL] Verification code issued for email={}", email);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[PlanWith] 이메일 인증번호 안내");
        message.setText("인증번호는 [" + code + "] 입니다. " + CODE_EXPIRE_MINUTES + "분 이내에 입력해주세요.");
        mailSender.send(message);
    }
}
