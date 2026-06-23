package com.gymplatform.gym.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardGymRequest(
        @NotBlank @Size(max = 150) String gymName,
        @NotBlank @Size(max = 150) String firstBranchName,
        String firstBranchAddress,
        @Valid GymAdminInfo admin
) {
    public record GymAdminInfo(
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            String phone
    ) {
    }
}
