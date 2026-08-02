package com.almahir.iti.dto.request;

import com.almahir.iti.model.enums.CircleType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CircleCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDateTime endDate,

        @NotNull CircleType type,
        boolean requiresApproval,
        @Positive Integer maxParticipants, // nullable = unlimited
        String password // required only if type == PRIVATE
) {
}