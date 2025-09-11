package com.playground.notification_manager.outbound.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class EmailMessageMapper {

    public SimpleMailMessage toSimpleMailMessage(EmailMessage emailMessage) {
        if (isInvalidEmailMessage(emailMessage)) {
            throw new IllegalArgumentException("EmailMessage and its fields ('to', 'from', 'subject', 'body') must not be null.");
        }

        var message = new SimpleMailMessage();
        message.setTo(emailMessage.getTo());
        message.setFrom(emailMessage.getFrom());
        message.setSubject(emailMessage.getSubject());
        message.setText(emailMessage.getBody());
        return message;
    }

    private boolean isInvalidEmailMessage(EmailMessage emailMessage) {
        return Objects.isNull(emailMessage)
                || Objects.isNull(emailMessage.getTo())
                || Objects.isNull(emailMessage.getFrom())
                || Objects.isNull(emailMessage.getSubject())
                || Objects.isNull(emailMessage.getBody());
    }
}