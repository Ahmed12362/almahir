package com.almahir.iti.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp

) {
}