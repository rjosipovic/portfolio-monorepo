package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.auth.AuthTokenResponse;
import com.studioengine.tutor.api.dto.auth.OtpRequest;
import com.studioengine.tutor.api.dto.auth.OtpVerificationRequest;
import com.studioengine.tutor.auth.AuthToken;
import com.studioengine.tutor.auth.OtpRequestCommand;
import com.studioengine.tutor.auth.OtpVerificationCommand;
import com.studioengine.tutor.auth.SecurityService;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.OtpVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private JacksonTester<OtpRequest> otpRequestJson;
    private JacksonTester<OtpVerificationRequest> otpVerificationRequestJson;
    private JacksonTester<AuthTokenResponse> authTokenResponseJson;
    private JacksonTester<ErrorResponse> errorResponseJson;

    @BeforeEach
    void setup() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class RequestOtpTests {

        @Test
        void shouldProcessOtpRequest() throws Exception {
            // given
            var email = "tutor.tutic@gmail.com";
            var request = OtpRequest.builder().email(email).build();

            // when
            var response = mockMvc.perform(post("/api/v1/auth/otp/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(otpRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            // then
            var captor = ArgumentCaptor.forClass(OtpRequestCommand.class);
            verify(securityService).requestOtp(captor.capture());
            var otpRequestCommand = captor.getValue();
            assertThat(otpRequestCommand.getEmail()).isEqualTo(email);

            assertThat(response.getContentAsString()).isEmpty();
        }

        @Test
        void shouldNotProcessOtpRequestWhenInvalidEmail() throws Exception {
            // given
            var email = "invalid-email";
            var request = OtpRequest.builder().email(email).build();

            // when
            mockMvc.perform(post("/api/v1/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(securityService, never()).requestOtp(any());
        }

        @Test
        void shouldNotProcessOtpRequestWhenEmailMissing() throws Exception {
            // given
            var request = OtpRequest.builder().build();

            // when
            mockMvc.perform(post("/api/v1/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(securityService, never()).requestOtp(any());
        }
    }

    @Nested
    class VerifyOtpTests {

        @Test
        void shouldReturnAuthToken() throws Exception {
            // given
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var request = OtpVerificationRequest.builder().email(email).otp(otp).build();
            var token = mock(AuthToken.class);
            var tokenValue = "token-value";
            var expiredIn = 3600L;

            var expectedResponse = AuthTokenResponse.builder().accessToken(tokenValue).expiresIn(expiredIn).build();

            when(securityService.verifyOtp(any(OtpVerificationCommand.class))).thenReturn(token);
            when(token.getAccessToken()).thenReturn(tokenValue);
            when(token.getExpiresIn()).thenReturn(expiredIn);

            // when
            var response = mockMvc.perform(post("/api/v1/auth/otp/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            // then
            var captor = ArgumentCaptor.forClass(OtpVerificationCommand.class);
            verify(securityService).verifyOtp(captor.capture());
            var otpVerificationCommand = captor.getValue();
            assertThat(otpVerificationCommand.getEmail()).isEqualTo(email);
            assertThat(otpVerificationCommand.getOtp()).isEqualTo(otp);

            assertThat(response.getContentAsString()).isEqualTo(authTokenResponseJson.write(expectedResponse).getJson());
        }

        @Test
        void shouldNotReturnAuthTokenWhenEmailMissing() throws Exception {
            // given
            var otp = "123456";
            var request = OtpVerificationRequest.builder().otp(otp).build();


            // when
            mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(securityService, never()).verifyOtp(any());
        }

        @Test
        void shouldNotReturnAuthTokenWhenEmailIsInvalid() throws Exception {
            // given
            var otp = "123456";
            var email = "invalid-email";
            var request = OtpVerificationRequest.builder().email(email).otp(otp).build();


            // when
            mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(securityService, never()).verifyOtp(any());
        }

        @Test
        void shouldNotReturnAuthTokenWhenOtpMissing() throws Exception {
            // given
            var email = "tutor.tutic@gmail.com";
            var request = OtpVerificationRequest.builder().email(email).build();


            // when
            mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(securityService, never()).verifyOtp(any());
        }

        @Test
        void shouldNotReturnAuthTokenWhenNotInstructorsEmail() throws Exception {
            // given
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var request = OtpVerificationRequest.builder().email(email).otp(otp).build();

            var reason = "Invalid credentials";
            when(securityService.verifyOtp(any())).thenThrow(new OtpVerificationException(reason));

            var expectedResponse = ErrorResponse.builder()
                    .code(ErrorCode.OTP_VERIFICATION_FAILED.getCode())
                    .message(ErrorCode.OTP_VERIFICATION_FAILED.getMessage())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andReturn().getResponse();

            // then
            verify(securityService).verifyOtp(any());

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedResponse).getJson());
        }

        @Test
        void shouldNotReturnAuthTokenWhenOtpExpired() throws Exception {
            // given
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var request = OtpVerificationRequest.builder().email(email).otp(otp).build();

            var reason = "OTP expired or not found";
            when(securityService.verifyOtp(any())).thenThrow(new OtpVerificationException(reason));

            var expectedResponse = ErrorResponse.builder()
                    .code(ErrorCode.OTP_VERIFICATION_FAILED.getCode())
                    .message(ErrorCode.OTP_VERIFICATION_FAILED.getMessage())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andReturn().getResponse();

            // then
            verify(securityService).verifyOtp(any());

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedResponse).getJson());
        }

        @Test
        void shouldNotReturnAuthTokenWhenInvalidOtp() throws Exception {
            // given
            var email = "tutor.tutic@gmail.com";
            var otp = "123456";
            var request = OtpVerificationRequest.builder().email(email).otp(otp).build();

            var reason = "Invalid OTP";
            when(securityService.verifyOtp(any())).thenThrow(new OtpVerificationException(reason));

            var expectedResponse = ErrorResponse.builder()
                    .code(ErrorCode.OTP_VERIFICATION_FAILED.getCode())
                    .message(ErrorCode.OTP_VERIFICATION_FAILED.getMessage())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(otpVerificationRequestJson.write(request).getJson()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andReturn().getResponse();

            // then
            verify(securityService).verifyOtp(any());

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedResponse).getJson());
        }
    }
}