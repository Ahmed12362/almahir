package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.SheikhStatus;

import java.util.UUID;

public record SheikhResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        SheikhStatus sheikhStatus,
        Double rate
) {
}
