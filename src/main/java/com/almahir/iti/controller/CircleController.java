package com.almahir.iti.controller;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.service.CircleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Circles", description = "Endpoints for managing Qur'an study circles and memberships")
@SecurityRequirement(name = "bearerAuth")
public class CircleController {

    private final CircleService circleService;

    @Operation(summary = "Create a new Circle", description = "Allows a Sheikh to create a new study circle.")
    @PostMapping("/circles")
    public ResponseEntity<ApiResponse<CircleResponse>> createCircle(
            @Valid @RequestBody CircleCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        CircleResponse circle = circleService.createCircle(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Circle created successfully", circle));
    }

    @Operation(summary = "List all Circles", description = "Retrieve a paginated list of circles, optionally filtered by status.")
    @GetMapping("/circles")
    public ResponseEntity<ApiResponse<Page<CircleResponse>>> listCircles(
            @Parameter(description = "Filter circles by status (e.g., SCHEDULED, ONGOING, COMPLETED, CANCELLED)")
            @RequestParam(required = false) CircleStatus status,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CircleResponse> page = circleService.listCircles(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Circles retrieved successfully", page));
    }

    @Operation(summary = "Get Circle details by ID", description = "Retrieve detailed information for a specific circle.")
    @GetMapping("/circles/{circleId}")
    public ResponseEntity<ApiResponse<CircleResponse>> getCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId
    ) {
        CircleResponse circle = circleService.getCircleById(circleId);
        return ResponseEntity.ok(ApiResponse.success("Circle details retrieved successfully", circle));
    }

    @Operation(summary = "Update Circle", description = "Allows the owning Sheikh to update circle details (name, dates).")
    @PatchMapping("/circles/{circleId}")
    public ResponseEntity<ApiResponse<CircleResponse>> updateCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Valid @RequestBody CircleUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        CircleResponse circle = circleService.updateCircle(currentUser, circleId, request);
        return ResponseEntity.ok(ApiResponse.success("Circle updated successfully", circle));
    }

    @Operation(summary = "Cancel Circle", description = "Soft-cancels a circle. Restricted to the owning Sheikh.")
    @DeleteMapping("/circles/{circleId}")
    public ResponseEntity<ApiResponse<Void>> cancelCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        circleService.cancelCircle(currentUser, circleId);
        return ResponseEntity.ok(ApiResponse.success("Circle cancelled successfully"));
    }

    @Operation(summary = "End Circle", description = "Ends an active circle and updates its status to COMPLETED. Restricted to the owning Sheikh.")
    @PostMapping("/circles/{circleId}/end")
    public ResponseEntity<ApiResponse<CircleEndResponse>> endCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        CircleEndResponse response = circleService.endCircle(circleId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Circle ended successfully", response));
    }

    @Operation(summary = "Request to Join a Circle", description = "Submits a join request for approval by the Sheikh. Fails if the user has a time overlap with an existing active circle.")
    @PostMapping("/circles/{circleId}/join")
    public ResponseEntity<ApiResponse<CircleJoinResponse>> joinCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        CircleJoinResponse joinResponse = circleService.joinCircle(circleId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Join request submitted successfully. Awaiting Sheikh approval.", joinResponse));
    }

    @Operation(summary = "Get Pending Join Requests", description = "Allows the owning Sheikh to view pending join requests for their circle.")
    @GetMapping("/circles/{circleId}/pending-requests")
    public ResponseEntity<ApiResponse<Page<PendingJoinRequestResponse>>> getPendingRequests(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PendingJoinRequestResponse> requests = circleService.getPendingRequests(circleId, currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending join requests retrieved successfully", requests));
    }

    @Operation(summary = "Approve Join Request", description = "Allows the owning Sheikh to approve a student's pending join request.")
    @PostMapping("/circles/{circleId}/approve/{userId}")
    public ResponseEntity<ApiResponse<CircleMemberResponse>> approveRequest(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(description = "UUID of the requesting user", required = true) @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        CircleMemberResponse member = circleService.approveJoinRequest(circleId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Member approved successfully", member));
    }

    @Operation(summary = "Reject Join Request", description = "Allows the owning Sheikh to reject a student's pending join request.")
    @PostMapping("/circles/{circleId}/reject/{userId}")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(description = "UUID of the requesting user", required = true) @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        circleService.rejectJoinRequest(circleId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Join request rejected"));
    }

    @Operation(summary = "Leave Circle", description = "Allows a user to leave a circle they are currently active or pending in.")
    @PostMapping("/circles/{circleId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        circleService.leaveCircle(circleId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Left circle successfully"));
    }

    @Operation(summary = "Get Circle Active Members", description = "Lists all active members in a circle.")
    @GetMapping("/circles/{circleId}/members")
    public ResponseEntity<ApiResponse<Page<CircleMemberResponse>>> getMembers(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CircleMemberResponse> members = circleService.getCircleMembers(circleId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    @Operation(summary = "Remove Member", description = "Allows the owning Sheikh to remove a member from the circle.")
    @DeleteMapping("/circles/{circleId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(description = "UUID of the user to remove", required = true) @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        circleService.removeMember(circleId, currentUser, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from circle"));
    }

    @Operation(summary = "Get My Active Circles", description = "Retrieve a paginated list of active circles for the logged-in user.")
    @GetMapping("/users/me/circles")
    public ResponseEntity<ApiResponse<Page<CircleResponse>>> getMyCircles(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CircleResponse> page = circleService.getMyCircles(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("User circles retrieved successfully", page));
    }
}