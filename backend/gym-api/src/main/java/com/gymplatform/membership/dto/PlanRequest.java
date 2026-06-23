package com.gymplatform.membership.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "MONTHLY|QUARTERLY|HALF_YEARLY|ANNUAL|CUSTOM") String planType,
        @NotNull @Positive Integer durationDays,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        String benefits
) {
}
