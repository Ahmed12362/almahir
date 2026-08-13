package com.almahir.iti.dto.request;

import com.almahir.iti.model.enums.Gender;
import com.almahir.iti.model.enums.SheikhStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateSheikhRequest(
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        String name,

        String firstName,

        String lastName,

        Gender gender,

        @Pattern(
                regexp = "^(\\+20|0)?1[0125]\\d{8}$",
                message = "Invalid Egyptian phone number format"
        )
        String phoneNumber,

        String profilePictureUrl,

        SheikhStatus sheikhStatus
) {
}
