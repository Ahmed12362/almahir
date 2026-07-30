package com.almahir.iti.dto.response;


import com.almahir.iti.model.enums.MeetingRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingRequestResponse(
        UUID requestId,
        MeetingRequestStatus status,
        String channelName,
        LocalDateTime expiresAt
) {
}