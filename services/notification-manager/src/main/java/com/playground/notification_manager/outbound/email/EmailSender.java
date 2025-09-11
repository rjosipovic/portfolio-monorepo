package com.playground.notification_manager.outbound.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private final JavaMailSender mailSender;
    private final EmailMessageMapper emailMessageMapper;

    public void sendEmail(EmailMessage emailMessage) {
        log.info("Sending email: {}", emailMessage);
        var message = emailMessageMapper.toSimpleMailMessage(emailMessage);
        mailSender.send(message);
    }
}
