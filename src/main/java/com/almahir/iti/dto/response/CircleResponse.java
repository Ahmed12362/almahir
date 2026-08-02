package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.CircleType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CircleResponse(
        UUID circleId,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        CircleStatus status,
        CircleType type,
        boolean requiresApproval,
        Integer maxParticipants,
        String channelName,
        UUID ownerId,
        long memberCount
) {
}