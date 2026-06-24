package com.gymplatform.member.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMemberRequest(
        @NotNull UUID branchId,
        @NotBlank @Size(max = 30) String admissionNumber,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 20) String phone,
        @Email String email,
        LocalDate dateOfBirth,
        @Pattern(regexp = "MALE|FEMALE|OTHER") String gender,
        @DecimalMin("0.0") BigDecimal heightCm,
        @DecimalMin("0.0") BigDecimal weightKg,
        String fitnessGoal,
        String profilePhotoUrl
) {
}
