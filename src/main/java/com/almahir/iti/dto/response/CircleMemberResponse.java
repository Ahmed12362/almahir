package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.MembershipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CircleMemberResponse(
        UUID id,
        String username,
        MembershipStatus status,
        LocalDateTime joinedAt
) {
}
