package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.Gender;

import java.util.UUID;

public record StudentResponse(
        UUID id,
        String firstName,
        String lastName,
        String username,
        Gender gender,
        String email,
        String phoneNumber,
        String profilePictureUrl
) {
}
