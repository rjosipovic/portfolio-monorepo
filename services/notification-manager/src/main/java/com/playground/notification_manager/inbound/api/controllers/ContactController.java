package com.playground.notification_manager.inbound.api.controllers;

import com.playground.notification_manager.inbound.api.dto.ContactMessage;
import com.playground.notification_manager.outbound.email.EmailMessage;
import com.playground.notification_manager.outbound.email.config.EmailConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class ContactController {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EmailConfig emailConfig;

    @PostMapping
    public ResponseEntity<Void> createNotification(@RequestBody @Valid ContactMessage contactMessage) {
        var to = emailConfig.getDefaultTo();
        var from = contactMessage.getFrom();
        var subject = contactMessage.getSubject();
        var body = contactMessage.getContent();
        var emailMessage = EmailMessage.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .body(body)
                .build();
        applicationEventPublisher.publishEvent(emailMessage);
        return ResponseEntity.accepted().build();
    }
}