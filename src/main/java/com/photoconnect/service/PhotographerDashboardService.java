package com.photoconnect.service;

import com.photoconnect.dto.PhotographerAnalyticsDto;
import com.photoconnect.dto.PhotographerDashboardView;

public interface PhotographerDashboardService {
    PhotographerDashboardView getDashboard(Long photographerUserId);

    PhotographerAnalyticsDto getAnalytics(Long photographerUserId);
}
