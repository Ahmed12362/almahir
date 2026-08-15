package com.almahir.iti.dto.response;

public record PaymobIntentionResponse(
        String id,
        String client_secret,
        String special_reference,
        String status,
        Boolean confirmed,
        String created
) {
}