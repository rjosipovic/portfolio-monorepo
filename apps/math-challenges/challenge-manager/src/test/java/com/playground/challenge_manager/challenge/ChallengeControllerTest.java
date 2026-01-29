package com.playground.challenge_manager.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.playground.challenge_manager.challenge.api.ApiPaths;
import com.playground.challenge_manager.challenge.api.controllers.ChallengeController;
import com.playground.challenge_manager.challenge.api.dto.AttemptRequest;
import com.playground.challenge_manager.challenge.api.dto.AttemptResponse;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeService;
import com.playground.challenge_manager.challenge.services.interfaces.UserIdentityService;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import com.playground.challenge_manager.challenge.services.model.commands.AttemptVerificationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.ChallengeCreationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.GetChallengeQuery;
import com.playground.challenge_manager.challenge.services.model.commands.SubscribeToChallengeCommand;
import com.playground.challenge_manager.config.StringToEnumConverterFactory;
import com.playground.challenge_manager.errors.advice.ControllerAdvice;
import com.playground.challenge_manager.errors.custom.ChallengeManagerError;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;
import com.playground.challenge_manager.errors.exceptions.specific.ChallengeDataCorruptedException;
import com.playground.challenge_manager.errors.exceptions.specific.ChallengeSubscriptionException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeController Unit Test")
class ChallengeControllerTest {

    @Mock
    private ChallengeService challengeService;

    @Mock
    private UserIdentityService userIdentityService;

    @InjectMocks
    private ChallengeController challengeController;

    private MockMvc mockMvc;

    private JacksonTester<ChallengeManagerError> jsonError;
    private JacksonTester<ChallengeResponse> jsonChallengeResponse;
    private JacksonTester<AttemptResponse> jsonAttemptResponse;

    private ObjectMapper objectMapper;

    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);

        var converterFactory = new StringToEnumConverterFactory();
        var conversionService = new FormattingConversionService();
        conversionService.addConverterFactory(converterFactory);

        mockMvc = MockMvcBuilders
                .standaloneSetup(challengeController)
                .setControllerAdvice(new ControllerAdvice())
                .setConversionService(conversionService)
                .build();
    }

    @Nested
    @DisplayName("POST " + ApiPaths.CHALLENGES)
    class CreateChallengeTests {

        @Test
        @DisplayName("Should return 202 Accepted with Location header")
        void testCreateChallenge() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var operation = OperationType.ADDITION;
            var difficulty = DifficultyLevel.MEDIUM;

            // Mock the user identity service
            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            // Mock the challenge service
            when(challengeService.create(any(ChallengeCreationCommand.class))).thenReturn(challengeId);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGES)
                            .param("difficulty", difficulty.name().toLowerCase())
                            .param("operation", operation.name().toLowerCase()))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.ACCEPTED.value(), response.getStatus()),
                    () -> assertTrue(Objects.requireNonNull(response.getHeader("Location")).endsWith(ApiPaths.CHALLENGES + "/" + challengeId))
            );
        }

        @Test
        @DisplayName("Should handle custom operand count")
        void testCreateChallenge_withCustomOperandCount() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var operation = OperationType.ADDITION;
            var difficulty = DifficultyLevel.EASY;
            var operandCount = 3;

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.create(any(ChallengeCreationCommand.class))).thenReturn(challengeId);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGES)
                            .param("difficulty", difficulty.name())
                            .param("operation", operation.name())
                            .param("operandCount", String.valueOf(operandCount)))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.ACCEPTED.value(), response.getStatus()),
                    () -> assertTrue(Objects.requireNonNull(response.getHeader("Location")).endsWith(ApiPaths.CHALLENGES + "/" + challengeId))
            );
        }

        @Test
        @DisplayName("Should return 400 for invalid difficulty")
        void createChallenge_whenDifficultyIsInvalid_shouldReturnValidationError() throws Exception {
            // given
            var invalidDifficulty = "foo";
            var operation = OperationType.ADDITION;

            var errorCode = ErrorCode.VALIDATION_FAILED;
            var allowedValues = Arrays.stream(DifficultyLevel.class.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            var expectedReason = String.format("Invalid value '%s' for parameter '%s'. Allowed values are: [%s].",
                    invalidDifficulty, "difficulty", allowedValues);
            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), expectedReason);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGES)
                            .param("difficulty", invalidDifficulty)
                            .param("operation", operation.name()))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }

        @Test
        @DisplayName("Should return 400 for invalid operation")
        void createChallenge_whenOperationIsInvalid_shouldReturnValidationError() throws Exception {
            // given
            var invalidOperation = "foo";
            var difficulty = DifficultyLevel.EASY;

            var errorCode = ErrorCode.VALIDATION_FAILED;
            var allowedValues = Arrays.stream(OperationType.class.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            var expectedReason = String.format("Invalid value '%s' for parameter '%s'. Allowed values are: [%s].",
                    invalidOperation, "operation", allowedValues);
            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), expectedReason);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGES)
                            .param("difficulty", difficulty.name())
                            .param("operation", invalidOperation))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }
    }

    @Nested
    @DisplayName("GET " + ApiPaths.CHALLENGES_WITH_ID)
    class GetChallengeTests {

        @Test
        @DisplayName("Should return 200 OK with challenge details")
        void getChallenge_Success() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var operands = List.of(1, 2);
            var operation = OperationType.ADDITION;
            var difficulty = DifficultyLevel.EASY;
            var expiredAt = ZonedDateTime.now().minusMinutes(5);

            var responseDto = ChallengeResponse.builder()
                    .id(challengeId)
                    .status(ChallengeStatus.PENDING)
                    .operands(operands)
                    .operation(operation)
                    .difficulty(difficulty)
                    .expiresAt(expiredAt)
                    .build();
            var query = GetChallengeQuery.builder()
                    .challengeId(challengeId)
                    .userId(testUserId)
                    .build();

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.getChallenge(query)).thenReturn(responseDto);

            // when
            var response = mockMvc
                    .perform(get(ApiPaths.CHALLENGES_WITH_ID, challengeId))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonChallengeResponse.write(responseDto).getJson(), response.getContentAsString())
            );
        }

        @Test
        @DisplayName("Should return 404 NOT_FOUND when challenge not found")
        void getChallenge_notFound() throws Exception {
            // given
            var challengeId = UUID.randomUUID();

            var query = GetChallengeQuery.builder()
                    .challengeId(challengeId)
                    .userId(testUserId)
                    .build();

            var errorCode = ErrorCode.NO_RESOURCE_FOUND;
            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), "Challenge not found");

            var exception = new EntityNotFoundException("Challenge not found");
            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.getChallenge(query)).thenThrow(exception);

            // when
            var response = mockMvc
                    .perform(get(ApiPaths.CHALLENGES_WITH_ID, challengeId))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }
    }

    @Nested
    @DisplayName("GET " + ApiPaths.CHALLENGE_STREAM)
    class StreamChallengeTests {

        @Test
        @DisplayName("Should return 200 OK and SseEmitter")
        void streamChallenge_Success() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var command = SubscribeToChallengeCommand.builder()
                    .challengeId(challengeId)
                    .userId(testUserId)
                    .build();
            var emitter = new SseEmitter();

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.subscribeToChallenge(command)).thenReturn(emitter);

            // when
            var response = mockMvc.perform(get(ApiPaths.CHALLENGE_STREAM, challengeId))
                    .andReturn().getResponse();

            // then
            assertEquals(HttpStatus.OK.value(), response.getStatus());
        }

        @Test
        @DisplayName("Should return 404 NOT_FOUND when subscription fails (challenge not found)")
        void streamChallenge_NotFound() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var command = SubscribeToChallengeCommand.builder()
                    .challengeId(challengeId)
                    .userId(testUserId)
                    .build();

            var errorCode = ErrorCode.CHALLENGE_NOT_FOUND_FOR_SUBSCRIPTION;
            var errorMessage = "Challenge not found or access denied";
            var exception = new ChallengeSubscriptionException(errorCode, errorMessage);
            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), errorMessage);

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.subscribeToChallenge(command)).thenThrow(exception);

            // when
            var response = mockMvc.perform(get(ApiPaths.CHALLENGE_STREAM, challengeId))
                    .andReturn().getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }
    }

    @Nested
    @DisplayName("POST " + ApiPaths.CHALLENGE_ATTEMPT)
    class SubmitAttemptTests {

        @Test
        @DisplayName("Should return 200 OK with attempt result")
        void submitAttempt_Success() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var guess = 5;
            var attemptRequest = new AttemptRequest(guess);

            var attemptResponse = AttemptResponse.builder()
                    .challengeId(challengeId)
                    .correct(true)
                    .status(ChallengeStatus.CORRECT)
                    .build();

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.submitAttempt(any(AttemptVerificationCommand.class))).thenReturn(attemptResponse);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGE_ATTEMPT, challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(attemptRequest)))
                    .andReturn()
                    .getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonAttemptResponse.write(attemptResponse).getJson(), response.getContentAsString())
            );
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid attempt body")
        void submitAttempt_InvalidBody() throws Exception {
            // given
            var challengeId = UUID.randomUUID();

            var errorCode = ErrorCode.VALIDATION_FAILED;
            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), "guess: Guess is required");

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGE_ATTEMPT, challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))// Empty JSON, guess is null
                    .andReturn()
                    .getResponse();

            // then
            assertAll(
                    () -> assertEquals(400, response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }

        @Test
        @DisplayName("Should return 409 Conflict when challenge state is invalid")
        void submitAttempt_Conflict() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var guess = 5;
            var attemptRequest = new AttemptRequest(guess);

            var errorCode = ErrorCode.CONFLICT;
            var errorMessage = "Challenge is not in valid state for verification";
            var exception = new ChallengeDataCorruptedException(errorCode, errorMessage);

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.submitAttempt(any(AttemptVerificationCommand.class))).thenThrow(exception);

            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), errorMessage);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGE_ATTEMPT, challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(attemptRequest)))
                    .andReturn()
                    .getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.CONFLICT.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error when challenge data is corrupted")
        void submitAttempt_InternalServerError() throws Exception {
            // given
            var challengeId = UUID.randomUUID();
            var guess = 5;
            var attemptRequest = new AttemptRequest(guess);

            var errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            var errorMessage = "Challenge has no correct answer";
            var exception = new ChallengeDataCorruptedException(errorCode, errorMessage);

            when(userIdentityService.getCurrentUserId()).thenReturn(testUserId);
            when(challengeService.submitAttempt(any(AttemptVerificationCommand.class))).thenThrow(exception);

            var expectedError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), errorMessage);

            // when
            var response = mockMvc.perform(post(ApiPaths.CHALLENGE_ATTEMPT, challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(attemptRequest)))
                    .andReturn()
                    .getResponse();

            // then
            assertAll(
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus()),
                    () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                    () -> assertEquals(jsonError.write(expectedError).getJson(), response.getContentAsString())
            );
        }
    }
}
