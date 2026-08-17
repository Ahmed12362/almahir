package com.almahir.iti.dto.response;

import java.time.Instant;

public record StudentSubscriptionMinutesResponse(
        String packageName,
        Integer totalMinutes,
        Integer remainingMinutes,
        Instant startedAt,
        Instant expiresAt
) {
}