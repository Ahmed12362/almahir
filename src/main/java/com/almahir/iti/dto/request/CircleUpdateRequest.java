package com.almahir.iti.dto.request;

import java.time.LocalDateTime;

public record CircleUpdateRequest(
        String name,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
