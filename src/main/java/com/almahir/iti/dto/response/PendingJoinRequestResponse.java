package com.almahir.iti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingJoinRequestResponse(
        UUID userId,
        String username,
        LocalDateTime requestedAt
) {}
