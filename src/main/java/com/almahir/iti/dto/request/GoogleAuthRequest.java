package com.almahir.iti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GoogleAuthRequest(
        @NotBlank(message = "idToken is required.")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$",
                message = "idToken is malformed."
        )
        String idToken
) {
}
