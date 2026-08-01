package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.MeetingRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentMeetingHistoryResponse(
        UUID requestId,
        UUID sheikhId,
        String sheikhName,
        MeetingRequestStatus status,
        LocalDateTime requestedAt,
        LocalDateTime acceptedAt,
        LocalDateTime endedAt
) {
}