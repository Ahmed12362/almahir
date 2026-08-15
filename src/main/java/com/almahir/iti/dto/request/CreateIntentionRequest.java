package com.almahir.iti.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateIntentionRequest(
        @NotBlank(message = "packageId is required.")
        String packageId,

        @NotBlank(message = "method is required.")
        String method,

        @NotBlank(message = "idempotencyKey is required.")
        String idempotencyKey
) {
}