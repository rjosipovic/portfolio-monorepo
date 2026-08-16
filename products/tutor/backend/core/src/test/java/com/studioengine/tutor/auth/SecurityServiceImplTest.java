package com.studioengine.tutor.auth;

import com.studioengine.tutor.config.AuthProperties;
import com.studioengine.tutor.dataaccess.entities.LoginAttempt;
import com.studioengine.tutor.dataaccess.entities.OtpRecord;
import com.studioengine.tutor.dataaccess.repositories.LoginAttemptRepository;
import com.studioengine.tutor.dataaccess.repositories.OtpRecordRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.OtpVerificationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private AuthProperties authProperties;
    @Mock
    private OtpRecordRepository otpRecordRepository;
    @Mock
    private LoginAttemptRepository loginAttemptRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private SecurityServiceImpl securityService;

    @Nested
    class RequestOtpTests {

        @Test
        void shouldSendOtp() {
            // given
            var email = "tutor.tutic@gmail.com";
            var otpLength = 6;
            var command = OtpRequestCommand.builder().email(email).build();

            when(authProperties.getInstructorEmail()).thenReturn(email);
            when(authProperties.getOtpLength()).thenReturn(otpLength);
            when(authProperties.getOtpExpiration()).thenReturn(Duration.ofMinutes(5));

            // when
            securityService.requestOtp(command);

            // then
            verify(authProperties).getInstructorEmail();
            verify(authProperties).getOtpLength();
            verify(authProperties).getOtpExpiration();

            var captor = ArgumentCaptor.forClass(OtpRecord.class);
            verify(otpRecordRepository).save(captor.capture());
            var otpRecordToSave = captor.getValue();
            assertThat(otpRecordToSave.getEmail()).isEqualTo(email);
            assertThat(otpRecordToSave.getOtpHash()).isNotBlank();
            assertThat(otpRecordToSave.getExpiresAt()).isInTheFuture();

            verify(emailService).sendOtpEmail(eq(email), anyString());
        }

        @Test
        void shouldNotSendOtpWenEmailNotRegistered() {
            // given
            var requestEmail = "tutor.wanabe@gmail.com";
            var tutorEmail = "tutor.tutic@gmail.com";
            var command = OtpRequestCommand.builder().email(requestEmail).build();

            when(authProperties.getInstructorEmail()).thenReturn(tutorEmail);

            // when
            securityService.requestOtp(command);

            // then
            verify(otpRecordRepository, never()).save(any());
            verify(emailService, never()).sendOtpEmail(any(), any());
        }
    }

    @Nested
    class VerifyOtpTests {

        @Test
        void shouldVerifyOtpAndGenerateAuthToken() {
            // given
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var maxAttempts = 5;
            var lockoutWindow = Duration.ofMinutes(30);
            var command = OtpVerificationCommand.builder().email(email).otp(otp).build();
            var otpRecord = mock(OtpRecord.class);

            when(authProperties.getInstructorEmail()).thenReturn(email);
            when(authProperties.getOtpLockoutWindow()).thenReturn(lockoutWindow);
            when(authProperties.getOtpMaxAttempts()).thenReturn(maxAttempts);
            when(loginAttemptRepository.countFailedAttemptsSince(eq(email), any(OffsetDateTime.class))).thenReturn(0L);
            when(otpRecordRepository.findByEmailAndUsedFalseAndExpiresAtAfter(eq(email), any(OffsetDateTime.class))).thenReturn(Optional.of(otpRecord));
            when(otpRecord.getOtpHash()).thenReturn(generateHash(otp));
            when(jwtProvider.generateToken(email)).thenReturn("token");
            when(authProperties.getJwtExpiration()).thenReturn(Duration.ofMinutes(60));

            // when
            var result = securityService.verifyOtp(command);

            // then
            verify(loginAttemptRepository).countFailedAttemptsSince(eq(email), any(OffsetDateTime.class));
            verify(otpRecordRepository).findByEmailAndUsedFalseAndExpiresAtAfter(eq(email), any(OffsetDateTime.class));
            verify(otpRecord).markUsed();

            var captor = ArgumentCaptor.forClass(LoginAttempt.class);
            verify(loginAttemptRepository).save(captor.capture());
            var loginAttemptToSave = captor.getValue();
            assertThat(loginAttemptToSave.getEmail()).isEqualTo(email);
            assertThat(loginAttemptToSave.isSuccessful()).isTrue();

            verify(jwtProvider).generateToken(email);

            assertThat(result.getAccessToken()).isNotBlank();
            assertThat(result.getExpiresIn()).isPositive();
        }

        @Test
        void shouldVerifyOtpAndNotGenerateAuthTokenWhenEmailNotRegistered() {
            // given
            var requestEmail = "tutor.wannabe@gmail.com";
            var tutorEmail = "tutor.tutic@gmail.com";
            var otp = "123456";
            var command = OtpVerificationCommand.builder().email(requestEmail).otp(otp).build();

            when(authProperties.getInstructorEmail()).thenReturn(tutorEmail);

            // when
            assertThatThrownBy(() -> securityService.verifyOtp(command)).isInstanceOf(OtpVerificationException.class);

            // then
            verify(loginAttemptRepository, never()).countFailedAttemptsSince(any(), any(OffsetDateTime.class));
            verify(otpRecordRepository, never()).findByEmailAndUsedFalseAndExpiresAtAfter(any(), any(OffsetDateTime.class));
        }

        @Test
        void shouldVerifyOtpAndNotGenerateAuthTokenWhenLocked() {
            // given
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var maxAttempts = 5;
            var lockoutWindow = Duration.ofMinutes(30);
            var command = OtpVerificationCommand.builder().email(email).otp(otp).build();

            when(authProperties.getInstructorEmail()).thenReturn(email);
            when(authProperties.getOtpLockoutWindow()).thenReturn(lockoutWindow);
            when(authProperties.getOtpMaxAttempts()).thenReturn(maxAttempts);
            when(loginAttemptRepository.countFailedAttemptsSince(eq(email), any(OffsetDateTime.class))).thenReturn(5L);

            // when
            assertThatThrownBy(() -> securityService.verifyOtp(command)).isInstanceOf(OtpVerificationException.class);

            //then
            verify(loginAttemptRepository).countFailedAttemptsSince(eq(email), any(OffsetDateTime.class));
            verify(otpRecordRepository, never()).findByEmailAndUsedFalseAndExpiresAtAfter(any(), any(OffsetDateTime.class));
        }

        @Test
        void shouldVerifyOtpAndNotGenerateAuthTokenWhenOtpNotFoundOrExpired() {
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var maxAttempts = 5;
            var lockoutWindow = Duration.ofMinutes(30);
            var command = OtpVerificationCommand.builder().email(email).otp(otp).build();

            when(authProperties.getInstructorEmail()).thenReturn(email);
            when(authProperties.getOtpLockoutWindow()).thenReturn(lockoutWindow);
            when(authProperties.getOtpLockoutWindow()).thenReturn(lockoutWindow);
            when(authProperties.getOtpMaxAttempts()).thenReturn(maxAttempts);
            when(loginAttemptRepository.countFailedAttemptsSince(eq(email), any(OffsetDateTime.class))).thenReturn(4L);
            when(otpRecordRepository.findByEmailAndUsedFalseAndExpiresAtAfter(eq(email), any(OffsetDateTime.class))).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> securityService.verifyOtp(command)).isInstanceOf(OtpVerificationException.class);

            //then
            verify(loginAttemptRepository).countFailedAttemptsSince(eq(email), any(OffsetDateTime.class));
            verify(otpRecordRepository).findByEmailAndUsedFalseAndExpiresAtAfter(eq(email), any(OffsetDateTime.class));
            verify(otpRecordRepository, never()).save(any());

            var captor = ArgumentCaptor.forClass(LoginAttempt.class);
            verify(loginAttemptRepository).save(captor.capture());
            assertThat(captor.getValue().isSuccessful()).isFalse();
        }

        @Test
        void shouldVerifyOtpAndNotGenerateAuthTokenWenOtpInvalid() {
            // given
            var email = "tutor.tutic@gmail.com";
            var requestOtp = "123456";
            var savedOtp = "654321";
            var maxAttempts = 5;
            var lockoutWindow = Duration.ofMinutes(30);
            var command = OtpVerificationCommand.builder().email(email).otp(requestOtp).build();
            var otpRecord = mock(OtpRecord.class);

            when(authProperties.getInstructorEmail()).thenReturn(email);
            when(authProperties.getOtpLockoutWindow()).thenReturn(lockoutWindow);
            when(authProperties.getOtpMaxAttempts()).thenReturn(maxAttempts);
            when(loginAttemptRepository.countFailedAttemptsSince(eq(email), any(OffsetDateTime.class))).thenReturn(0L);
            when(otpRecordRepository.findByEmailAndUsedFalseAndExpiresAtAfter(eq(email), any(OffsetDateTime.class))).thenReturn(Optional.of(otpRecord));
            when(otpRecord.getOtpHash()).thenReturn(generateHash(savedOtp));

            // when
            assertThatThrownBy(() -> securityService.verifyOtp(command)).isInstanceOf(OtpVerificationException.class);

            // then
            verify(loginAttemptRepository).countFailedAttemptsSince(eq(email), any(OffsetDateTime.class));
            verify(otpRecordRepository).findByEmailAndUsedFalseAndExpiresAtAfter(eq(email), any(OffsetDateTime.class));
            verify(otpRecord, never()).markUsed();

            var captor = ArgumentCaptor.forClass(LoginAttempt.class);
            verify(loginAttemptRepository).save(captor.capture());
            var loginAttemptToSave = captor.getValue();
            assertThat(loginAttemptToSave.getEmail()).isEqualTo(email);
            assertThat(loginAttemptToSave.isSuccessful()).isFalse();

            verify(otpRecordRepository, never()).save(any());
        }

        private String generateHash(String otp) {
            try {
                var digest = MessageDigest.getInstance("SHA-256");
                var hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("SHA-256 not available", ex);
            }
        }
    }
}