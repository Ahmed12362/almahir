package com.almahir.iti.dto.request;

public record LoginRequest(
        String email,
        String password
){}