package com.almahir.iti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CircleMemberResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        LocalDateTime joinedAt
) {
}
