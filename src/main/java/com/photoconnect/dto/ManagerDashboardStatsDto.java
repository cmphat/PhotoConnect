package com.photoconnect.dto;

public record ManagerDashboardStatsDto(
        long pendingPhotographerApplications,
        long openReports,
        long unresolvedDisputes,
        long openSupportTickets,
        long totalBookings
) {
    public long getPendingPhotographerApplications() {
        return pendingPhotographerApplications;
    }

    public long getOpenReports() {
        return openReports;
    }

    public long getUnresolvedDisputes() {
        return unresolvedDisputes;
    }

    public long getOpenSupportTickets() {
        return openSupportTickets;
    }

    public long getTotalBookings() {
        return totalBookings;
    }
}
