package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.Gender;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        Gender gender,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        String provider,
        Set<String> roles
) {
}
