package com.almahir.iti.dto.response;

import java.time.Instant;

public record CreateIntentionResponse(
        String intentionId,
        String clientSecret,
        String publicKey,
        Long amountMinorUnits,
        String currencyCode,
        Instant expiresAt
) {
}