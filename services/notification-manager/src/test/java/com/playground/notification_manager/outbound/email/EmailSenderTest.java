package com.playground.notification_manager.outbound.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private EmailMessageMapper emailMessageMapper;

    @InjectMocks
    private EmailSender emailSender;

    @Test
    @DisplayName("Should map and send email when input is valid")
    void sendEmail_whenInputIsValid_sendsCorrectMail() {
        // given
        var defaultFrom = "no-reply@example.com";
        var to = "recipient@example.com";
        var subject = "Test Subject";
        var body = "Test Body";
        var emailMessage = EmailMessage.builder()
                .from(defaultFrom)
                .to(to)
                .subject(subject)
                .body(body)
                .build();
        var simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(defaultFrom);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        when(emailMessageMapper.toSimpleMailMessage(emailMessage)).thenReturn(simpleMailMessage);

        //when
        emailSender.sendEmail(emailMessage);

        //then
        verify(javaMailSender, times(1)).send(captor.capture());
        var sentMessage = captor.getValue();

        assertAll(
                () -> assertThat(sentMessage.getFrom()).isEqualTo(defaultFrom),
                () -> assertThat(sentMessage.getTo()).containsExactly(to),
                () -> assertThat(sentMessage.getSubject()).isEqualTo(subject),
                () -> assertThat(sentMessage.getText()).isEqualTo(body)
        );
    }

    @Test
    @DisplayName("Should propagate exception and not send email when mapper fails")
    void sendEmail_whenMapperThrowsException_propagatesException() {
        // given
        var emailMessage = EmailMessage.builder().build(); // Content doesn't matter for this test
        var expectedException = new IllegalArgumentException("Invalid email message");
        when(emailMessageMapper.toSimpleMailMessage(emailMessage)).thenThrow(expectedException);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> emailSender.sendEmail(emailMessage));

        // verify that the mail sender was never called
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should propagate exception when mail sender fails")
    void sendEmail_whenMailSenderThrowsException_propagatesException() {
        // given
        var emailMessage = EmailMessage.builder().to("t").from("f").subject("s").body("b").build();
        var simpleMailMessage = new SimpleMailMessage();
        var expectedException = new MailSendException("Failed to connect to mail server");

        when(emailMessageMapper.toSimpleMailMessage(emailMessage)).thenReturn(simpleMailMessage);
        doThrow(expectedException).when(javaMailSender).send(simpleMailMessage);

        // when & then
        assertThrows(MailSendException.class, () -> emailSender.sendEmail(emailMessage));
    }
}