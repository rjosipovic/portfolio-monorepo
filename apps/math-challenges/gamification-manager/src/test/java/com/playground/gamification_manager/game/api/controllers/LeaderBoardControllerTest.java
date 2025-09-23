package com.playground.gamification_manager.game.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.interfaces.LeaderBoardService;
import com.playground.gamification_manager.game.service.model.LeaderBoardItem;
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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class LeaderBoardControllerTest {

    private MockMvc mvc;

    @Mock
    private LeaderBoardService leaderBoardService;

    @InjectMocks
    private LeaderBoardController controller;

    JacksonTester<List<LeaderBoardItem>> jsonLeaderBoard;

    @BeforeEach
    void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldGetLeaderBoard() throws Exception {
        //given
        var first = LeaderBoardItem.builder()
                .alias("player1")
                .totalScore(750)
                .badges(Set.of(BadgeType.FIRST_WON, BadgeType.BRONZE, BadgeType.SILVER, BadgeType.GOLD))
                .build();
        var second = LeaderBoardItem.builder()
                .alias("player2")
                .totalScore(500)
                .badges(Set.of(BadgeType.FIRST_WON, BadgeType.BRONZE, BadgeType.SILVER))
                .build();
        var third = LeaderBoardItem.builder()
                .alias("player3")
                .totalScore(250)
                .badges(Set.of(BadgeType.FIRST_WON, BadgeType.BRONZE))
                .build();
        var leaderBoard = List.of(first, second, third);
        when(leaderBoardService.getLeaderBoard()).thenReturn(leaderBoard);

        //when
        var response = mvc.perform(get("/leaders"))
                .andReturn().getResponse();
        //then
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                () -> assertEquals(jsonLeaderBoard.write(leaderBoard).getJson(), response.getContentAsString())
        );
    }
}