package com.gymplatform.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID gymId,
        UUID memberId,
        String type,
        String channel,
        String status,
        String payload,
        Instant sentAt,
        Instant createdAt
) {
}
