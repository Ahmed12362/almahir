package com.almahir.iti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CircleCreateRequest(
        @NotBlank(message = "Name is required.")
        String name,

        @NotNull(message = "startDate is required.")
        LocalDateTime startDate,

        @NotNull(message = "endDate is required.")
        LocalDateTime endDate
) {
}