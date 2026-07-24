package com.almahir.iti.dto.response;

public record TafsirCatalogResponse(
        String tafsirKey,
        String displayName,
        String language,
        String languageName,
        String downloadUrl,
        Long fileSizeBytes
) {
}