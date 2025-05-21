package com.choi.springmall2.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    @DisplayName("이메일 전송 테스트")
    void sendPasswordResetToken() {
        // given
        String toEmail = "test@example.com";
        String token = "test-token";
        String domainUrl = "https://example.com";

        // Value 설정 대신 임의 값 주입
        ReflectionTestUtils.setField(mailService, "domainUrl", domainUrl);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // when
        mailService.sendPasswordResetToken(toEmail, token);

        // then
        verify(javaMailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertTrue(sentMessage.getText().contains(domainUrl + "/reset-password?token=" + token));
        assertEquals("Springmall2 비밀번호 재설정", sentMessage.getSubject());
    }
}