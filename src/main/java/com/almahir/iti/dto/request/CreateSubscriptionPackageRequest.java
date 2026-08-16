package com.almahir.iti.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Details used by an administrator to create a subscription package")
public record CreateSubscriptionPackageRequest(
        @NotBlank(message = "Package code is required")
        @Size(max = 100, message = "Package code must not exceed 100 characters")
        @Schema(example = "pkg-light")
        String code,

        @NotBlank(message = "Package name is required")
        @Size(max = 150, message = "Package name must not exceed 150 characters")
        @Schema(example = "Light Package")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(example = "Suitable for occasional sessions")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        @Schema(description = "Price in the currency's minor units", example = "15000")
        Long priceMinorUnits,

        @NotBlank(message = "Currency code is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must be a 3-letter ISO code")
        @Schema(example = "EGP")
        String currencyCode,

        @NotNull(message = "Allowed meeting minutes is required")
        @Positive(message = "Allowed meeting minutes must be greater than zero")
        @Schema(example = "120")
        Integer meetingMinutesAllowed,

        @Positive(message = "Duration days must be greater than zero")
        @Schema(description = "Optional subscription validity period in days", example = "30")
        Integer durationDays,

        @Schema(example = "[\"Priority booking\", \"Session reminders\"]")
        Set<@NotBlank(message = "A feature cannot be blank") @Size(max = 255, message = "A feature must not exceed 255 characters") String> features,

        @Schema(description = "Whether customers can purchase this package", example = "true", defaultValue = "true")
        Boolean active
) {
}
