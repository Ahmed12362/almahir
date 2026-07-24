package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.CircleStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CircleResponse(
        UUID id,
        String name,
        LocalDateTime startDate,
        LocalDateTime endDate,
        CircleStatus status,
        long memberCount
) {
}
