package com.gymplatform.membership.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMembershipRequest(
        @NotNull UUID memberId,
        @NotNull UUID planId,
        @NotNull LocalDate startDate,
        @DecimalMin("0.0") BigDecimal discountAmount
) {
}
