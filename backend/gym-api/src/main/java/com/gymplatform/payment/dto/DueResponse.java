package com.gymplatform.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DueResponse(
        UUID membershipId,
        String status,
        BigDecimal pendingAmount,
        LocalDate nextDueDate,
        Long remainingDays,
        Long overdueDays,
        LocalDate membershipExpiryDate
) {
}
