package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.CircleStatus;
import java.time.LocalDateTime;

public record CircleEndResponse(
        CircleStatus status,
        LocalDateTime endedAt
) {}