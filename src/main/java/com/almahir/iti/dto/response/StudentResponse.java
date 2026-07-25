package com.almahir.iti.dto.response;

import java.util.UUID;

public record StudentResponse(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        String profilePictureUrl
) {
}