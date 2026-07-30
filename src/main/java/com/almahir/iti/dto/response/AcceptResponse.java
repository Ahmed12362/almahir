package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.MeetingRequestStatus;

import java.util.UUID;

public record AcceptResponse(
        MeetingRequestStatus status,
        UUID requestId,
        String channelName,
        String agoraToken,
        String userAccount
) {
}