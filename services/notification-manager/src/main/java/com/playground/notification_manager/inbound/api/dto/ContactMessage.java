package com.playground.notification_manager.inbound.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ContactMessage {

    @Email
    @NotNull
    String from;
    @NotBlank
    String subject;
    @NotBlank
    String content;
}
