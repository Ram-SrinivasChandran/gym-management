package com.gymplatform.report.dto;

import java.util.Map;

public record MembershipReportResponse(
        long totalMemberships,
        Map<String, Long> countByStatus,
        Map<String, Long> countByPlanName
) {
}
