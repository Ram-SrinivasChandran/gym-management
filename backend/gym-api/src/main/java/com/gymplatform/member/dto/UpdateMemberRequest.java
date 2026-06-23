package com.gymplatform.member.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateMemberRequest(
        @Size(max = 150) String fullName,
        @Size(max = 20) String phone,
        @Email String email,
        LocalDate dateOfBirth,
        @Pattern(regexp = "MALE|FEMALE|OTHER") String gender,
        @DecimalMin("0.0") BigDecimal heightCm,
        @DecimalMin("0.0") BigDecimal weightKg,
        String fitnessGoal,
        String profilePhotoUrl
) {
}
