package com.photoconnect.service;

import com.photoconnect.dto.AdminDashboardStatsDto;

public interface AdminDashboardService {

    /**
     * Aggregates platform metrics, user counts, booking totals, reviews, and deposit stats.
     * Uses efficient database count and sum operations without loading entity sets into memory.
     */
    AdminDashboardStatsDto getDashboardStats();
}
