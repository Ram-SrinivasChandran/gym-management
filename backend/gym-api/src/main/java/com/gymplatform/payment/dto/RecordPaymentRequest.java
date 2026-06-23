package com.gymplatform.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentRequest(
        @NotNull UUID membershipId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull @Pattern(regexp = "FULL|PARTIAL|ADVANCE") String paymentType,
        @NotNull @Pattern(regexp = "CASH|CARD|UPI|BANK_TRANSFER|OTHER") String paymentMethod
) {
}
