package com.studioengine.tutor.api.mapper;

import com.studioengine.tutor.api.dto.landing.DashboardOverviewResponse;
import com.studioengine.tutor.landing.DashboardOverview;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LandingMapper {

    DashboardOverviewResponse toDashboardOverviewResponse(DashboardOverview dashboardOverview);
}
