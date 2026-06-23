package com.gymplatform.gym.dto;

import java.time.Instant;
import java.util.UUID;

public record GymResponse(
        UUID id,
        String name,
        String subscriptionTier,
        String status,
        Instant createdAt
) {
}
