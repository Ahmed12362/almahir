package com.almahir.iti.dto.request;

public record RegisterRequest(
        String username,
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber
) {
}