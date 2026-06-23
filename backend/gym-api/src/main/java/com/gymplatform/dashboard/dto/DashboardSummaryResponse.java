package com.gymplatform.dashboard.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalMembers,
        long newRegistrationsThisMonth,
        Map<String, Long> membershipsByStatus,
        long dueTodayCount,
        long dueThisWeekCount,
        long overdueCount,
        BigDecimal revenueThisMonth,
        long paymentCountThisMonth,
        long renewalsThisMonth,
        long attendanceTodayCount
) {
}
