package com.gymplatform.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BroadcastRequest(
        @NotBlank String message,
        @Pattern(regexp = "PUSH|SMS|EMAIL|IN_APP") String channel
) {
}
