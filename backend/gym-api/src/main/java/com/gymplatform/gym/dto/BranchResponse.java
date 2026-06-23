package com.gymplatform.gym.dto;

import java.time.Instant;
import java.util.UUID;

public record BranchResponse(
        UUID id,
        UUID gymId,
        String name,
        String address,
        String status,
        Instant createdAt
) {
}
