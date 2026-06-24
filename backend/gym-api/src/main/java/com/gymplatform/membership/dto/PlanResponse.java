package com.gymplatform.membership.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        UUID gymId,
        String name,
        String planType,
        Integer durationDays,
        BigDecimal price,
        BigDecimal discountAmount,
        String benefits,
        String status,
        Instant createdAt
) {
}
