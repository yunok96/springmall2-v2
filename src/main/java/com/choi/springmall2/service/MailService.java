package com.choi.springmall2.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${app.domain.url}")
    private String domainUrl;

    private final JavaMailSender javaMailSender;

    /**
     * 비밀번호 초기화 인증 메일 전송
     * @param toEmail 이메일
     * @param token 비밀번호 초기화 토큰
     */
    public void sendPasswordResetToken(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Springmall2 비밀번호 재설정");

        String resetUrl = domainUrl + "/reset-password?token=" + token;
        message.setText("아래 링크를 클릭하면 비밀번호 재설정 화면으로 이동합니다:\n" + resetUrl);

        javaMailSender.send(message);
    }
}
