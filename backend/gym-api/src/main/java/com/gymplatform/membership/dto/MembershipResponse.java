package com.gymplatform.membership.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MembershipResponse(
        UUID id,
        UUID memberId,
        UUID planId,
        UUID renewedFromId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPrice,
        String status,
        Instant createdAt
) {
}
