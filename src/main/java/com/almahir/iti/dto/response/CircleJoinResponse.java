package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.MembershipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CircleJoinResponse(
        UUID membershipId,
        UUID circleId,
        UUID userId,
        MembershipStatus status,
        LocalDateTime requestedAt
) {
}
