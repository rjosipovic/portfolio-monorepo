package com.playground.user_manager.auth.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.playground.user_manager.auth.api.dto.AuthCodeGenerationRequest;
import com.playground.user_manager.auth.api.dto.AuthCodeVerificationRequest;
import com.playground.user_manager.auth.api.dto.RegisterUserRequest;
import com.playground.user_manager.auth.api.dto.RegisteredUser;
import com.playground.user_manager.auth.service.AuthService;
import com.playground.user_manager.auth.service.JwtGenerator;
import com.playground.user_manager.auth.service.RegistrationService;
import com.playground.user_manager.errors.advice.ControllerAdvice;
import com.playground.user_manager.errors.custom.UserManagerError;
import com.playground.user_manager.errors.exceptions.UserAlreadyExistsException;
import com.playground.user_manager.errors.exceptions.UserNotFoundException;
import com.playground.user_manager.errors.exceptions.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    @Mock
    private AuthService authService;
    @Mock
    private JwtGenerator jwtGenerator;
    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private AuthController controller;

    private JacksonTester<AuthCodeGenerationRequest> authCodeGenerationRequestJacksonTester;
    private JacksonTester<AuthCodeVerificationRequest> authCodeVerificationRequestJacksonTester;
    private JacksonTester<RegisterUserRequest> registerUserDTOTester;
    private JacksonTester<UserManagerError> errorJacksonTester;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ControllerAdvice())
                .build();
    }

    @Test
    void requestCode_happyPath_returnsOk() throws Exception {
        //given
        var email = "user@example.com";
        var request = AuthCodeGenerationRequest.builder().email(email).build();
        doNothing().when(authService).generateAuthCode(email);

        //when
        var result = mockMvc.perform(post("/auth/request-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authCodeGenerationRequestJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(200, result.getStatus())
        );
    }

    @Test
    void requestCode_invalidEmail_returnsBadRequest() throws Exception {
        //given
        var email = "not-an-email";
        var request = AuthCodeGenerationRequest.builder().email(email).build();
        var errorCode = ErrorCode.VALIDATION_FAILED;

        //when
        var result = mockMvc.perform(post("/auth/request-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authCodeGenerationRequestJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        var resContent = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), "email: must be a well-formed email address");
        //then
        assertAll(
                () -> assertEquals(400, result.getStatus()),
                () -> assertEquals("application/json", result.getContentType()),
                () -> assertEquals("UTF-8", result.getCharacterEncoding()),
                () -> assertEquals(errorJacksonTester.write(resContent).getJson(), result.getContentAsString())
        );
    }

    @Test
    void requestCode_userNotFound_returnsNotFound() throws Exception {
        //given
        var email = "user@example.com";
        var request = AuthCodeGenerationRequest.builder().email(email).build();
        doThrow(new UserNotFoundException(String.format("User with email %s is not registered", email))).when(authService).generateAuthCode(email);
        var errorCode = ErrorCode.USER_NOT_FOUND;

        //when
        var result = mockMvc.perform(post("/auth/request-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authCodeGenerationRequestJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        var resContent = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), String.format("User with email %s is not registered", email));
        //then
        assertAll(
                () -> assertEquals(404, result.getStatus()),
                () -> assertEquals("application/json", result.getContentType()),
                () -> assertEquals("UTF-8", result.getCharacterEncoding()),
                () -> assertEquals(errorJacksonTester.write(resContent).getJson(), result.getContentAsString())
        );
    }

    @Test
    void verifyCode_happyPath_returnsOk() throws Exception {
        //given
        var email = "user@example.com";
        var alias = "test-alias";
        var userId = UUID.randomUUID().toString();
        var registeredUser = RegisteredUser.builder()
                .userId(userId)
                .email(email)
                .alias(alias)
                .build();
        var code = "123456";
        var token = "token";
        var request = AuthCodeVerificationRequest.builder().email(email).code(code).build();
        when(authService.verifyCode(email, code)).thenReturn(true);
        when(jwtGenerator.generate(registeredUser)).thenReturn(token);
        when(registrationService.getRegisteredUser(email)).thenReturn(Optional.of(registeredUser));

        //when
        var result = mockMvc.perform(post("/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authCodeVerificationRequestJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(200, result.getStatus()),
                () -> assertEquals("application/json", result.getContentType()),
                () -> assertEquals("UTF-8", result.getCharacterEncoding()),
                () -> assertEquals("{\"token\":\"token\"}", result.getContentAsString())
        );
    }

    @Test
    void verifyCode_invalidCode_returnsBadRequest() throws Exception {
        //given
        var email = "user@example.com";
        var code = "123456";
        var request = AuthCodeVerificationRequest.builder().email(email).code(code).build();
        when(authService.verifyCode(email, code)).thenReturn(false);
        var errorCode = ErrorCode.INVALID_VERIFICATION_CODE;

        //when
        var result = mockMvc.perform(post("/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authCodeVerificationRequestJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        var resContent = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), String.format("Code verification failed for email: %s and code: %s", email, code));
        //then
        assertAll(
                () -> assertEquals(400, result.getStatus()),
                () -> assertEquals("application/json", result.getContentType()),
                () -> assertEquals("UTF-8", result.getCharacterEncoding()),
                () -> assertEquals(errorJacksonTester.write(resContent).getJson(), result.getContentAsString())
        );
    }

    @Test
    void verifyCode_missingFields_returnsBadRequest() throws Exception {
        //given
        var email = "user@example.com";
        var request = AuthCodeVerificationRequest.builder().email(email).build();
        var errorCode = ErrorCode.VALIDATION_FAILED;

        //when
        var result = mockMvc.perform(post("/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authCodeVerificationRequestJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        var resContent = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), "code: must not be blank");
        //then
        assertAll(
                () -> assertEquals(400, result.getStatus()),
                () -> assertEquals("application/json", result.getContentType()),
                () -> assertEquals("UTF-8", result.getCharacterEncoding()),
                () -> assertEquals(errorJacksonTester.write(resContent).getJson(), result.getContentAsString())
        );
    }

    @Test
    void testRegisterUser_withMandatoryFields() throws Exception {
        //given
        var alias = "test-user";
        var email = "someemail@gmail.com";

        //when
        var res = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserDTOTester.write(RegisterUserRequest.builder().alias(alias).email(email).build()).getJson()))
                .andReturn().getResponse();

        //then
        assertEquals(200, res.getStatus());
    }

    @Test
    void testRegisterUser_failure_alreadyExists() throws Exception {
        //given
        var alias = "test-user";
        var email = "someemail@gmail.com";
        var registerUser = RegisterUserRequest.builder()
                .alias(alias)
                .email(email)
                .build();
        var reason = "User with alias " + alias + " already exists";
        var ex = new UserAlreadyExistsException(reason);
        var errorCode = ErrorCode.USER_ALREADY_EXISTS;

        doThrow(ex).when(registrationService).register(registerUser);

        var error = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), reason);

        //when
        var res = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserDTOTester.write(registerUser).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(400, res.getStatus()),
                () -> assertEquals("application/json", res.getContentType()),
                () -> assertEquals("UTF-8", res.getCharacterEncoding()),
                () -> assertEquals(errorJacksonTester.write(error).getJson(), res.getContentAsString())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRegisterUserProvider")
    void testRegisterUser_validationFailure(RegisterUserRequest request, String expectedReason) throws Exception {
        //given
        var errorCode = ErrorCode.VALIDATION_FAILED;
        var expectedError = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), expectedReason);

        //when
        var res = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserDTOTester.write(request).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(400, res.getStatus()),
                () -> assertEquals("application/json", res.getContentType()),
                () -> assertEquals(errorJacksonTester.write(expectedError).getJson(), res.getContentAsString())
        );
    }

    private static Stream<Arguments> invalidRegisterUserProvider() {
        return Stream.of(
                Arguments.of(RegisterUserRequest.builder().email("test@test.com").build(), "alias: must not be blank"),
                Arguments.of(RegisterUserRequest.builder().alias("alias").build(),"email: must not be blank"),
                Arguments.of(RegisterUserRequest.builder().alias("alias").email("invalid-email").build(), "email: must be a well-formed email address"),
                Arguments.of(RegisterUserRequest.builder().alias("alias").email("test@test.com").birthdate(LocalDate.now().plusDays(1)).build(), "birthdate: must be a past date"),
                Arguments.of(RegisterUserRequest.builder().alias("alias").email("test@test.com").gender("invalid").build(), "gender: must match \"male|female\"")
        );
    }
}