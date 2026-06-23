package com.gymplatform.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        UUID gymId,
        UUID branchId,
        String fullName,
        String email,
        String phone,
        String role,
        String status,
        Instant createdAt
) {
}
