package com.almahir.iti.dto.response;

import com.almahir.iti.model.SheikhStatus;

import java.util.UUID;

/**
 * Search result for a Sheikh. Match positions use the half-open interval [startIndex, endIndex).
 * Both positions are null when no search term was supplied.
 */
public record SheikhSearchResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl,
        SheikhStatus sheikhStatus,
        Double rate,
        Integer startIndex,
        Integer endIndex
) {
}
