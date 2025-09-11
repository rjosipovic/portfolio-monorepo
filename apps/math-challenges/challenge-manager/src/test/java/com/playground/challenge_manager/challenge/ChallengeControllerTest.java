package com.playground.challenge_manager.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.challenge_manager.challenge.api.controllers.ChallengeController;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import com.playground.challenge_manager.challenge.services.model.Challenge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class ChallengeControllerTest {

    @Mock
    private ChallengeGeneratorService challengeGeneratorService;

    @InjectMocks
    private ChallengeController challengeController;

    private MockMvc mockMvc;

    private JacksonTester<Challenge> jsonChallenge;

    @BeforeEach
    void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
        mockMvc = MockMvcBuilders
                .standaloneSetup(challengeController)
                .build();
    }

    @Test
    void testGetRandomChallenge() throws Exception {
        //given
        var challenge = Challenge.builder()
                .firstNumber(11)
                .secondNumber(12)
                .build();
        var difficulty = "medium";
        when(challengeGeneratorService.randomChallenge(difficulty)).thenReturn(challenge);

        //when then
        var response = mockMvc.perform(get("/challenges/random"))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                () -> assertEquals(jsonChallenge.write(challenge).getJson(), response.getContentAsString())
        );
    }
}