package com.almahir.iti.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ErrorResponse(
        boolean success,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp

) {
}