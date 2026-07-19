package com.almahir.iti.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp

) {
}