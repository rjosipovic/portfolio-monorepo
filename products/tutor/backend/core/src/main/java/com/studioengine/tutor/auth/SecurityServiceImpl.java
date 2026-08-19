package com.studioengine.tutor.auth;

import com.studioengine.tutor.config.AuthProperties;
import com.studioengine.tutor.dataaccess.entities.LoginAttempt;
import com.studioengine.tutor.dataaccess.entities.OtpRecord;
import com.studioengine.tutor.dataaccess.repositories.LoginAttemptRepository;
import com.studioengine.tutor.dataaccess.repositories.OtpRecordRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.OtpVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    private final AuthProperties authProperties;
    private final OtpRecordRepository otpRecordRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final EmailService emailService;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public void requestOtp(OtpRequestCommand command) {
        var email = command.getEmail();

        if (isNotInstructorsEmail(email)) {
            log.warn("OTP request for unregistered email, ignoring");
            return;
        }

        if (isLockedOut(email)) {
            log.warn("OTP request for locked account, ignoring");
            return;
        }

        otpRecordRepository.invalidateAllByEmail(email);

        var otp = generateOtp();
        var otpHash = hashOtp(otp);
        var expiresAt = OffsetDateTime.now().plus(authProperties.getOtpExpiration());

        var otpRecord = OtpRecord.create(email, otpHash, expiresAt);
        otpRecordRepository.save(otpRecord);

        emailService.sendOtpEmail(email, otp);

        log.info("OTP requested for {}", email);
    }

    @Override
    public AuthToken verifyOtp(OtpVerificationCommand command) {
        var email = command.getEmail();
        var otp = command.getOtp();

        if (isNotInstructorsEmail(email)) {
            throw new OtpVerificationException("Invalid credentials");
        }

        checkLockout(email);

        var otpRecord = otpRecordRepository.findByEmailAndUsedFalseAndExpiresAtAfter(email, OffsetDateTime.now())
                .orElseThrow(() -> new OtpVerificationException("OTP expired or not found"));

        if (!verifyHash(otp, otpRecord.getOtpHash())) {
            recordFailedAttempt(email);
            throw new OtpVerificationException("Invalid OTP");
        }

        otpRecord.markUsed();
        otpRecordRepository.save(otpRecord);
        recordSuccessAttempt(email);

        var token = jwtProvider.generateToken(email);
        var expiresIn = authProperties.getJwtExpiration().toSeconds();

        log.info("OTP verified for {}, JWT issued", email);

        return AuthToken.builder()
                .accessToken(token)
                .expiresIn(expiresIn)
                .build();
    }

    private boolean isNotInstructorsEmail(String email) {
        return !Objects.equals(email, authProperties.getInstructorEmail());
    }

    private String generateOtp() {
        var random = new SecureRandom();
        var length = authProperties.getOtpLength();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String hashOtp(String otp) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 not available", ex);
        }
    }

    private boolean verifyHash(String otp, String storedHash) {
        var requestOtpHashBytes = hashOtp(otp).getBytes(StandardCharsets.UTF_8);
        var storedHashBytes = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(requestOtpHashBytes, storedHashBytes);
    }

    private void checkLockout(String email) {
        if (isLockedOut(email)) {
            throw new OtpVerificationException("Account locked. Try again later");
        }
    }

    private boolean isLockedOut(String email) {
        var since = OffsetDateTime.now().minus(authProperties.getOtpLockoutWindow());
        var failedAttempts = loginAttemptRepository.countFailedAttemptsSince(email, since);
        return failedAttempts >= authProperties.getOtpMaxAttempts();
    }

    private void recordFailedAttempt(String email) {
        loginAttemptRepository.save(LoginAttempt.create(email, false));
    }

    private void recordSuccessAttempt(String email) {
        loginAttemptRepository.save(LoginAttempt.create(email, true));
    }
}
