package com.almahir.iti.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "A subscription package available to customers")
public record SubscriptionPackageResponse(
        UUID id,
        String code,
        String name,
        String description,
        Long priceMinorUnits,
        String currencyCode,
        Integer meetingMinutesAllowed,
        Integer durationDays,
        Set<String> features,
        boolean active
) {
}
