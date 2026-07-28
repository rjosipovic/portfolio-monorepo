package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.landing.DashboardOverviewResponse;
import com.studioengine.tutor.api.mapper.LandingMapper;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.landing.DashboardOverview;
import com.studioengine.tutor.landing.LandingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardLandingControllerTest {

    @Mock
    private LandingService landingService;

    @Mock
    private LandingMapper landingMapper;

    @InjectMocks
    private DashboardLandingController dashboardLandingController;

    private MockMvc mockMvc;

    private JacksonTester<DashboardOverviewResponse> dashboardOverviewResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardLandingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnDashboardOverview() throws Exception {
        // given
        var dashboardOverview = mock(DashboardOverview.class);
        var actionItem = DashboardOverviewResponse.ActionItemResponse.builder()
                .appointmentId(UUID.randomUUID())
                .studentName("Marko Markić")
                .serviceCategory("Primary school Math")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0))
                .amount(BigDecimal.TEN)
                .build();
        var dashboardOverviewResponse = DashboardOverviewResponse.builder()
                .awaitingClosure(List.of(actionItem))
                .pendingPayments(List.of())
                .todayUpcoming(List.of())
                .build();

        when(landingService.getLandingPageData()).thenReturn(dashboardOverview);
        when(landingMapper.toDashboardOverviewResponse(dashboardOverview)).thenReturn(dashboardOverviewResponse);

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/landing"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(landingService).getLandingPageData();
        verify(landingMapper).toDashboardOverviewResponse(dashboardOverview);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(dashboardOverviewResponseJson.write(dashboardOverviewResponse).getJson());
    }

    @Test
    void shouldReturnEmptyDashboardOverview() throws Exception {
        // given
        var dashboardOverview = mock(DashboardOverview.class);
        var dashboardOverviewResponse = DashboardOverviewResponse.builder()
                .awaitingClosure(List.of())
                .pendingPayments(List.of())
                .todayUpcoming(List.of())
                .build();

        when(landingService.getLandingPageData()).thenReturn(dashboardOverview);
        when(landingMapper.toDashboardOverviewResponse(dashboardOverview)).thenReturn(dashboardOverviewResponse);

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/landing"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(landingService).getLandingPageData();
        verify(landingMapper).toDashboardOverviewResponse(dashboardOverview);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(dashboardOverviewResponseJson.write(dashboardOverviewResponse).getJson());
    }
}
