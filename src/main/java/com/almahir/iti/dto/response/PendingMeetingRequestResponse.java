package com.almahir.iti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingMeetingRequestResponse(
        UUID requestId,
        UUID studentId,
        String studentName,
        String studentEmail,
        LocalDateTime requestedAt,
        LocalDateTime expiresAt
) {
}