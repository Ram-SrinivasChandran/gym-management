package com.gymplatform.member.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberDocumentResponse(
        UUID id,
        UUID memberId,
        String docType,
        String fileUrl,
        Instant uploadedAt
) {
}
