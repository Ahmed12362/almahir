package com.almahir.iti.dto.response;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        String provider,
        Set<String> roles
) {
}
