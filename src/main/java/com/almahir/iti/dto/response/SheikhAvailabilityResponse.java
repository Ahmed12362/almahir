package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.SheikhStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SheikhAvailabilityResponse(
        UUID sheikhId,
        SheikhStatus status,
        LocalDateTime updatedAt
) {
}