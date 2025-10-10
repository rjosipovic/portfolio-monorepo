package com.playground.notification_manager.mappers;

import com.playground.notification_manager.inbound.api.dto.ContactMessage;
import com.playground.notification_manager.inbound.messaging.auth.AuthNotification;
import com.playground.notification_manager.outbound.email.EmailMessage;
import com.playground.notification_manager.outbound.email.config.EmailConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "dto.content", target = "body")
    @Mapping(target = "to", expression = "java(config.getDefaultTo())")
    @Mapping(source = "dto.from", target = "from")
    @Mapping(source = "dto.subject", target = "subject")
    EmailMessage toEmailMessage(ContactMessage dto, @Context EmailConfig config);

    @Mapping(target = "from", expression = "java(config.getDefaultFrom())")
    @Mapping(source = "dto.to", target = "to")
    @Mapping(source = "dto.subject", target = "subject")
    @Mapping(source = "dto.body", target = "body")
    EmailMessage toEmailMessage(AuthNotification dto, @Context EmailConfig config);
}
