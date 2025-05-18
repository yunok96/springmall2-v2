package com.choi.springmall2.event.listener;

import com.choi.springmall2.event.PasswordResetMailEvent;
import com.choi.springmall2.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MailEventListener {
    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetMail(PasswordResetMailEvent event) {
        mailService.sendPasswordResetToken(event.email(), event.token());
    }
}
