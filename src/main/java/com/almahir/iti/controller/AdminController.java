package com.almahir.iti.controller;

import com.almahir.iti.dto.request.CreateSubscriptionPackageRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.enums.PaymentStatus;
import com.almahir.iti.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Administrative operations. All endpoints require an authenticated user with the ADMIN role.")
@SecurityRequirement(name = "BearerAuthentication")
public class AdminController {
    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a sheikh", description = "Changes the sheikh's status to AVAILABLE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sheikh approved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin role required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Sheikh not found", content = @Content)
    })
    @PostMapping("/sheikhs/{sheikhId}/approve")
    public ResponseEntity<ApiResponse<SheikhResponse>> approve(@Parameter(description = "Sheikh UUID", required = true) @PathVariable UUID sheikhId) {
        return ResponseEntity.ok(ApiResponse.success("Sheikh approved", adminService.approveSheikh(sheikhId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Decline a sheikh", description = "Declines the sheikh and blocks the linked user account.")
    @PostMapping("/sheikhs/{sheikhId}/decline")
    public ResponseEntity<ApiResponse<SheikhResponse>> decline(@PathVariable UUID sheikhId) {
        return ResponseEntity.ok(ApiResponse.success("Sheikh declined", adminService.declineSheikh(sheikhId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block a user", description = "Blocks the user and revokes all active refresh-token sessions.")
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<ApiResponse<UserResponse>> block(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User blocked", adminService.blockUser(userId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unblock a user", description = "Restores access for a blocked user.")
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<ApiResponse<UserResponse>> unblock(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User unblocked", adminService.unblockUser(userId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a subscription package", description = "Creates a package that can be purchased by customers when active. The package code must be unique.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Subscription package created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid package data", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin role required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Package code already exists", content = @Content)
    })
    @PostMapping("/subscription-packages")
    public ResponseEntity<ApiResponse<SubscriptionPackageResponse>> createSubscriptionPackage(
            @Valid @RequestBody CreateSubscriptionPackageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Subscription package created",
                adminService.createSubscriptionPackage(request)
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List payment transactions",
            description = """
                    Returns a paginated list of all payment transactions with the paying user
                    and the purchased package. Supports optional filtering by status,
                    userId, and a createdAt date range.
                    """
    )
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentTransactionAdminResponse>>> getPaymentTransactions(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment transactions retrieved",
                adminService.getPaymentTransactions(status, userId, from, to, pageable)
        ));
    }
}
