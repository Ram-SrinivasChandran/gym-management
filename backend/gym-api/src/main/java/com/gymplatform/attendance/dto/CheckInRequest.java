package com.gymplatform.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record CheckInRequest(
        @NotNull UUID memberId,
        @Pattern(regexp = "MANUAL|QR") String method
) {
}
