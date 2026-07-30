package com.almahir.iti.dto.request;

import com.almahir.iti.model.enums.SheikhStatus;
import jakarta.validation.constraints.NotNull;

public record SheikhAvailabilityRequest(
        @NotNull(message = "Status cannot be null")
        SheikhStatus status
) {
}