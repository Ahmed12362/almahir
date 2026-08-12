package com.almahir.iti.controller;

import com.almahir.iti.dto.request.CreateSheikhReviewRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.SheikhReviewResponse;
import com.almahir.iti.model.AuthUser;
import com.almahir.iti.service.SheikhReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sheikh")
@RequiredArgsConstructor
@Tag(name = "Sheikh Reviews", description = "Endpoints for rating and reviewing sheikhs")
@SecurityRequirement(name = "bearerAuth")
public class SheikhReviewController {

    private final SheikhReviewService sheikhReviewService;

    @Operation(summary = "Add review for a Sheikh", description = "Allows a student to rate and review a Sheikh once.")
    @PostMapping("/{sheikhId}/reviews")
    public ResponseEntity<ApiResponse<SheikhReviewResponse>> addReview(
            @Parameter(description = "Sheikh UUID")
            @PathVariable UUID sheikhId,
            @Valid @RequestBody CreateSheikhReviewRequest request
    ) {
        AuthUser currentUser = getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(
                "Review added successfully",
                sheikhReviewService.addReview(currentUser.getUser(), sheikhId, request)
        ));
    }

    @Operation(summary = "Get Sheikh reviews", description = "Retrieves all reviews for a Sheikh.")
    @GetMapping("/{sheikhId}/reviews")
    public ResponseEntity<ApiResponse<List<SheikhReviewResponse>>> getReviews(
            @Parameter(description = "Sheikh UUID")
            @PathVariable UUID sheikhId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikh reviews retrieved successfully",
                sheikhReviewService.getReviews(sheikhId)
        ));
    }

    private AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthUser) authentication.getPrincipal();
    }
}
