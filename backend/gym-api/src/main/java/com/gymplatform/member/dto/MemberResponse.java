package com.gymplatform.member.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        UUID gymId,
        UUID branchId,
        String memberCode,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        BigDecimal heightCm,
        BigDecimal weightKg,
        BigDecimal bmi,
        String fitnessGoal,
        String profilePhotoUrl,
        String status,
        Instant createdAt
) {
}
