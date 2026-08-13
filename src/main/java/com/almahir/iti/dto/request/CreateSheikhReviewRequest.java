package com.almahir.iti.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSheikhReviewRequest(
        @NotNull
        @Min(1)
        @Max(5)
        Integer rate,
        String comment
) {
}
