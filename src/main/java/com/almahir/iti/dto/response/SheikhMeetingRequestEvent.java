package com.almahir.iti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SheikhMeetingRequestEvent(
        UUID requestId,
        UUID studentId,
        String studentName,
        String note,
        LocalDateTime expiresAt
) {
}