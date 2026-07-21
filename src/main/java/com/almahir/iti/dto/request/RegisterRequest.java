package com.almahir.iti.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        String username,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @Email
        @NotBlank
        String email,

        @Size(min = 8)
        String password,

        @Pattern(
                regexp = "^(\\+20|0)?1[0125]\\d{8}$",
                message = "Invalid Egyptian phone number format"
        )
        String phoneNumber

) {
}