package com.playground.notification_manager.outbound.email;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailListener {

    private final EmailSender emailSender;

    @EventListener
    public void onEmailMessage(EmailMessage emailMessage) {
        emailSender.sendEmail(emailMessage);
    }
}
