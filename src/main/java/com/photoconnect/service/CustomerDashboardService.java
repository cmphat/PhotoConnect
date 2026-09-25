package com.photoconnect.service;

import com.photoconnect.dto.CustomerDashboardView;

public interface CustomerDashboardService {
    CustomerDashboardView getDashboard(Long customerUserId);
}
