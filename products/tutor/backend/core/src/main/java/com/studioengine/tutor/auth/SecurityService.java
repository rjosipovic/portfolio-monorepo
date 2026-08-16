package com.studioengine.tutor.auth;

public interface SecurityService {

    void requestOtp(OtpRequestCommand command);

    AuthToken verifyOtp(OtpVerificationCommand command);
}
