package com.almahir.iti.dto.response;

public record AdminAuthResponse(
        String accessToken,
        String refreshToken
) {}
