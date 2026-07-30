package com.almahir.iti.dto.response;

public record AgoraTokenResponse(
        String token,
        String channelName,
        String userAccount
) {
}