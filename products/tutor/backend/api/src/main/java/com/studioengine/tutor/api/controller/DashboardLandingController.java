package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.landing.DashboardOverviewResponse;
import com.studioengine.tutor.api.mapper.LandingMapper;
import com.studioengine.tutor.landing.LandingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard/landing")
@RequiredArgsConstructor
@Slf4j
public class DashboardLandingController {

    private final LandingService landingService;
    private final LandingMapper landingMapper;

    @GetMapping
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        log.info("GET /dashboard/landing/dashboard-overview");
        var result = landingService.getLandingPageData();
        var response = landingMapper.toDashboardOverviewResponse(result);
        return ResponseEntity.ok(response);
    }
}
