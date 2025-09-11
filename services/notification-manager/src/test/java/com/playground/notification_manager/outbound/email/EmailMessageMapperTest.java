package com.playground.notification_manager.outbound.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailMessageMapperTest {

    private EmailMessageMapper emailMessageMapper;

    @BeforeEach
    void setUp() {
        emailMessageMapper = new EmailMessageMapper();
    }

    @Test
    @DisplayName("Should correctly map a valid EmailMessage to SimpleMailMessage")
    void shouldMapCorrectlyWhenMessageIsValid() {
        // given
        var emailMessage = EmailMessage.builder()
                .to("recipient@example.com")
                .from("sender@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        // when
        var result = emailMessageMapper.toSimpleMailMessage(emailMessage);

        // then
        assertNotNull(result);
        assertAll("Should map all fields correctly",
                () -> assertArrayEquals(new String[]{"recipient@example.com"}, result.getTo()),
                () -> assertEquals("sender@example.com", result.getFrom()),
                () -> assertEquals("Test Subject", result.getSubject()),
                () -> assertEquals("Test Body", result.getText())
        );
    }

    @DisplayName("Should throw IllegalArgumentException for any invalid input")
    @ParameterizedTest
    @MethodSource("provideInvalidEmailMessages")
    void shouldThrowIllegalArgumentExceptionForInvalidInput(EmailMessage invalidMessage) {
        assertThrows(IllegalArgumentException.class,
                () -> emailMessageMapper.toSimpleMailMessage(invalidMessage));
    }

    private static Stream<EmailMessage> provideInvalidEmailMessages() {
        return Stream.of(
                null, // Test case for a completely null EmailMessage
                EmailMessage.builder().from("f").subject("s").body("b").build(), // 'to' is null
                EmailMessage.builder().to("t").subject("s").body("b").build(),   // 'from' is null
                EmailMessage.builder().to("t").from("f").body("b").build(),      // 'subject' is null
                EmailMessage.builder().to("t").from("f").subject("s").build()    // 'body' is null
        );
    }
}