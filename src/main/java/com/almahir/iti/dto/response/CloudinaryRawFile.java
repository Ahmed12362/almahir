package com.almahir.iti.dto.response;

public record CloudinaryRawFile(
        String publicId,
        String secureUrl,
        long bytes
) {
}