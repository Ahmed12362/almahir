package com.almahir.iti.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CircleUpdateRequest(
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Future(message = "Start date must be in the future")
        LocalDateTime startDate,

        @Future(message = "End date must be in the future")
        LocalDateTime endDate
) {
}