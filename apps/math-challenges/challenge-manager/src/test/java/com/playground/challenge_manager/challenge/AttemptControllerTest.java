package com.playground.challenge_manager.challenge;

import com.playground.challenge_manager.auth.AuthConfig;
import com.playground.challenge_manager.auth.JwtUserPrincipal;
import com.playground.challenge_manager.challenge.api.controllers.AttemptController;
import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;
import com.playground.challenge_manager.challenge.services.interfaces.AttemptService;
import com.playground.challenge_manager.config.ManagementConfig;
import com.playground.challenge_manager.config.SecurityConfig;
import com.playground.challenge_manager.errors.custom.ChallengeManagerError;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureJsonTesters
@WebMvcTest(AttemptController.class)
@Import({SecurityConfig.class, ManagementConfig.class})
class AttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttemptService challengeService;
    @MockitoBean
    private AuthConfig authConfig;

    @Autowired
    private JacksonTester<ChallengeAttemptDTO> jsonChallengeAttempt;
    @Autowired
    private JacksonTester<ChallengeResultDTO> jsonChallengeResult;
    @Autowired
    private JacksonTester<ChallengeManagerError> jsonChallengeManagerError;


    @Test
    void testMakeAttempt() throws Exception {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 276;
        var correct = 276;
        var game = "multiplication";
        var attempt = ChallengeAttemptDTO.builder()
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var attemptWithUserId = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var result = ChallengeResultDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .correctResult(correct)
                .correct(true)
                .game(game)
                .build();
        when(challengeService.verifyAttempt(attemptWithUserId)).thenReturn(result);
        var principal = JwtUserPrincipal.builder()
                .claim("userId", userId.toString())
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        //when
        var response = mockMvc.perform(post("/attempts")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChallengeAttempt.write(attempt).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                () -> assertEquals(jsonChallengeResult.write(result).getJson(), response.getContentAsString())
        );
    }

    @Test
    void testMakeAttempt_Failure() throws Exception {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 6789;
        var correct = 276;
        var game = "multiplication";
        var attempt = ChallengeAttemptDTO.builder()
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var attemptWithUserId = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var result = ChallengeResultDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .correctResult(correct)
                .correct(false)
                .game(game)
                .build();
        when(challengeService.verifyAttempt(attemptWithUserId)).thenReturn(result);
        var principal = JwtUserPrincipal.builder()
                .claim("userId", userId.toString())
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, null);

        //when
        var response = mockMvc.perform(post("/attempts")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChallengeAttempt.write(attempt).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                () -> assertEquals(jsonChallengeResult.write(result).getJson(), response.getContentAsString())
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAttemptData")
    void testMakeAttempt_invalidInput(Integer firstNumber, Integer secondNumber, Integer guess, String game, String expectedReason) throws Exception {
        //given
        var userId = UUID.randomUUID();
        var attempt = ChallengeAttemptDTO.builder()
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var principal = JwtUserPrincipal.builder()
                .claim("userId", userId.toString())
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, null);
        var errorCode = ErrorCode.VALIDATION_FAILED;

        //when
        var response = mockMvc.perform(post("/attempts")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChallengeAttempt.write(attempt).getJson()))
                .andReturn().getResponse();

        var resContent = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), expectedReason);
        //then
        assertAll(
                () -> assertEquals(400, response.getStatus()),
                () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                () -> assertEquals(jsonChallengeManagerError.write(resContent).getJson(), response.getContentAsString())
        );
    }

    private static Stream<Arguments> provideInvalidAttemptData() {
        return Stream.of(
                Arguments.of(null, 23, 6789, "multiplication", "firstNumber: must not be null"),
                Arguments.of(12, null, 6789, "multiplication", "secondNumber: must not be null"),
                Arguments.of(12, 23, 6789, null, "game: must not be null"),
                Arguments.of(12, 23, 6789, "invalidGame", "game: must match \"addition|subtraction|multiplication|division\""),
                Arguments.of(12, 23, null, "multiplication", "guess: must not be null"),
                Arguments.of(12, 345, 357, "addition", "Numbers must have the same digit count")
        );
    }
}