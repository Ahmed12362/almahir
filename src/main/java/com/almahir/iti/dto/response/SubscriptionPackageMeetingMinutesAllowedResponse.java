package com.almahir.iti.dto.response;

import java.util.Set;

public record SubscriptionPackageMeetingMinutesAllowedResponse(
        String code,
        String name,
        String description,
        Long priceMinorUnits,
        String currencyCode,
        Integer meetingMinutesAllowed,
        Integer durationDays,
        Set<String> features
) {
}
