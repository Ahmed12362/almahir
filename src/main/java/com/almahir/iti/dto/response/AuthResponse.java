package com.almahir.iti.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean isNewUser,
        UserResponse user
) {

}