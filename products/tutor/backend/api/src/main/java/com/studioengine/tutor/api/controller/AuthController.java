package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.auth.AuthTokenResponse;
import com.studioengine.tutor.api.dto.auth.OtpRequest;
import com.studioengine.tutor.api.dto.auth.OtpVerificationRequest;
import com.studioengine.tutor.auth.OtpRequestCommand;
import com.studioengine.tutor.auth.OtpVerificationCommand;
import com.studioengine.tutor.auth.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SecurityService securityService;

    @PostMapping("/otp/request")
    public ResponseEntity<Void> requestOtp(@RequestBody @Valid OtpRequest request) {
        log.info("POST /auth/otp/request");

        var email = request.getEmail();
        var command = OtpRequestCommand.builder().email(email).build();
        securityService.requestOtp(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthTokenResponse> verifyOpt(@RequestBody @Valid OtpVerificationRequest request) {
        log.info("POST /auth/otp/verify");

        var email = request.getEmail();
        var otp = request.getOtp();
        var command = OtpVerificationCommand.builder().email(email).otp(otp).build();
        var result = securityService.verifyOtp(command);
        var accessToken = result.getAccessToken();
        var expiresIn = result.getExpiresIn();
        var response = AuthTokenResponse.builder().accessToken(accessToken).expiresIn(expiresIn).build();

        return ResponseEntity.ok(response);
    }
}
