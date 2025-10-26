package com.playground.analytics_manager.outbound.api.controllers;

import com.playground.analytics_manager.config.ManagementConfig;
import com.playground.analytics_manager.config.SecurityConfig;
import com.playground.analytics_manager.outbound.api.dto.UserSuccessRate;
import com.playground.analytics_manager.outbound.auth.AuthConfig;
import com.playground.analytics_manager.outbound.auth.JwtUserPrincipal;
import com.playground.analytics_manager.outbound.errors.exceptions.UserNotFoundException;
import com.playground.analytics_manager.outbound.services.user_statistics.UserStatisticsService;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureJsonTesters
@WebMvcTest(StatisticsController.class)
@Import({SecurityConfig.class, ManagementConfig.class})
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserStatisticsService userStatisticsService;
    @MockitoBean
    private AuthConfig authConfig;

    @Autowired
    private JacksonTester<UserSuccessRate> jsonUserSuccessRate;

    @Test
    void returnsSuccessRate() throws Exception {
        //given
        var userId = UUID.randomUUID().toString();
        var userSuccessRate = buildUserSuccessRate();
        when(userStatisticsService.getUserStatistics(userId)).thenReturn(userSuccessRate);
        var principal = JwtUserPrincipal.builder()
                .claim("userId", userId)
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        //when
        var response = mockMvc.perform(get("/analytics/statistics")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUserSuccessRate.write(userSuccessRate).getJson()))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType()),
                () -> assertEquals(jsonUserSuccessRate.write(userSuccessRate).getJson(), response.getContentAsString())
        );
    }

    @Test
    void returnsNotFound() throws Exception {
        //given
        var userId = UUID.randomUUID().toString();
        when(userStatisticsService.getUserStatistics(userId)).thenThrow(new UserNotFoundException(String.format("User %s not found", userId)));
        var principal = JwtUserPrincipal.builder()
                .claim("userId", userId)
                .build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        //when
        var response = mockMvc.perform(get("/analytics/statistics")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        //then
        assertAll(
                () -> assertEquals(404, response.getStatus())
        );
    }

    private static UserSuccessRate buildUserSuccessRate() {
        var alias = "alias";
        var userSuccessRate = new UserSuccessRate(alias);
        userSuccessRate.processSuccessAttempt("addition", "easy");
        userSuccessRate.processSuccessAttempt("addition", "easy");
        userSuccessRate.processSuccessAttempt("addition", "easy");
        userSuccessRate.processFailAttempt("addition", "easy");
        return userSuccessRate;
    }
}