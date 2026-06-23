package com.gymplatform.attendance.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        UUID memberId,
        LocalDate attendanceDate,
        Instant checkInAt,
        Instant checkOutAt,
        String method
) {
}
