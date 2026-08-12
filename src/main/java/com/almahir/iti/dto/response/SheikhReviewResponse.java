package com.almahir.iti.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SheikhReviewResponse(
        UUID id,
        UUID sheikhId,
        UUID studentId,
        String studentUsername,
        Integer rate,
        String comment,
        Instant createdAt
) {
}
