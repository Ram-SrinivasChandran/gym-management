package com.gymplatform.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID membershipId,
        BigDecimal amount,
        String paymentType,
        String paymentMethod,
        String receiptNumber,
        boolean reversed,
        UUID recordedBy,
        Instant paidAt
) {
}
