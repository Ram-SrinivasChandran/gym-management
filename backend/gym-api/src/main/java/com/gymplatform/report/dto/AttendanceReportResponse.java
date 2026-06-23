package com.gymplatform.report.dto;

import java.time.LocalDate;
import java.util.Map;

public record AttendanceReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        long totalCheckIns,
        Map<LocalDate, Long> checkInsByDate
) {
}
