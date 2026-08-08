package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.MeetingRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SheikhMeetingHistoryResponse(
        UUID requestId,
        UUID studentId,
        String studentName,
        MeetingRequestStatus status,
        LocalDateTime requestedAt,
        LocalDateTime acceptedAt,
        LocalDateTime endedAt
) {
}