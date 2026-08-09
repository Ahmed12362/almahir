package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.Gender;
import com.almahir.iti.model.enums.SheikhStatus;

import java.util.UUID;

public record SheikhResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        Gender gender,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        SheikhStatus sheikhStatus,
        Double rate
) {
}
