package com.almahir.iti.dto.response;

import java.util.Set;

public record UserResponse(
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String provider,
        Set<String> roles
) {
}
