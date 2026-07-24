package com.almahir.iti.dto.response;

import com.almahir.iti.model.enums.TafsirBuildStatus;

public record TafsirBuildStatusResponse(
        String tafsirKey,
        String language,
        TafsirBuildStatus status,
        String fileUrl,
        Long fileSizeBytes
) {
}