package com.almahir.iti.controller;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleJoinRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.AuthUser;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.service.CircleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/circles")
@RequiredArgsConstructor
@Tag(name = "Circles", description = "Endpoints for managing Qur'an study circles and memberships")
@SecurityRequirement(name = "bearerAuth")
public class CircleController {

    private final CircleService circleService;

    @Operation(summary = "Create a new Circle", description = "Allows a Sheikh to create a new study circle.")
    @PostMapping()
    public ResponseEntity<ApiResponse<CircleResponse>> createCircle(
            @Valid @RequestBody CircleCreateRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CircleResponse circle = circleService.createCircle(authUser.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Circle created successfully", circle));
    }

    @Operation(summary = "List all Circles", description = "Retrieve a paginated list of circles, optionally filtered by status.")
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<CircleResponse>>> listCircles(
            @Parameter(description = "Filter circles by status (e.g., SCHEDULED, ONGOING, COMPLETED, CANCELLED)")
            @RequestParam(required = false) CircleStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "startDate") Pageable pageable
    ) {
        Page<CircleResponse> page = circleService.listCircles(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Circles retrieved successfully", page));
    }

    @Operation(summary = "Get Circle details by ID", description = "Retrieve detailed information for a specific circle.")
    @GetMapping("/{circleId}")
    public ResponseEntity<ApiResponse<CircleResponse>> getCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId
    ) {
        CircleResponse circle = circleService.getCircleById(circleId);
        return ResponseEntity.ok(ApiResponse.success("Circle details retrieved successfully", circle));
    }

    @Operation(summary = "Update Circle", description = "Allows the owning Sheikh to update circle details (name, dates).")
    @PatchMapping("/{circleId}")
    public ResponseEntity<ApiResponse<CircleResponse>> updateCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Valid @RequestBody CircleUpdateRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CircleResponse circle = circleService.updateCircle(authUser.getUser(), circleId, request);
        return ResponseEntity.ok(ApiResponse.success("Circle updated successfully", circle));
    }

    @Operation(summary = "Cancel Circle", description = "Soft-cancels a circle. Restricted to the owning Sheikh.")
    @DeleteMapping("/{circleId}")
    public ResponseEntity<ApiResponse<Void>> cancelCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        circleService.cancelCircle(authUser.getUser(), circleId);
        return ResponseEntity.ok(ApiResponse.success("Circle cancelled successfully"));
    }

    @Operation(summary = "End Circle", description = "Ends an active circle and updates its status to COMPLETED. Restricted to the owning Sheikh.")
    @PostMapping("/{circleId}/end")
    public ResponseEntity<ApiResponse<CircleEndResponse>> endCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CircleEndResponse response = circleService.endCircle(circleId, authUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Circle ended successfully", response));
    }

    @Operation(summary = "Request to Join a Circle", description = "Submits a join request. For PRIVATE circles, a valid password must be provided. Fails if the user has a time overlap with an existing active circle.")
    @PostMapping("/{circleId}/join")
    public ResponseEntity<ApiResponse<CircleJoinResponse>> joinCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody(required = false) CircleJoinRequest request
    ) {
        CircleJoinResponse joinResponse = circleService.joinCircle(circleId, authUser.getUser(), request);
        return ResponseEntity.ok(ApiResponse.success("Join request submitted successfully.", joinResponse));
    }

    @Operation(summary = "Get Pending Join Requests", description = "Allows the owning Sheikh to view pending join requests for their circle.")
    @GetMapping("/{circleId}/pending-requests")
    public ResponseEntity<ApiResponse<Page<PendingJoinRequestResponse>>> getPendingRequests(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @AuthenticationPrincipal AuthUser authUser,
            @ParameterObject @PageableDefault(size = 20, sort = "joinedAt") Pageable pageable
    ) {
        Page<PendingJoinRequestResponse> requests = circleService.getPendingRequests(circleId, authUser.getUser(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending join requests retrieved successfully", requests));
    }

    @Operation(summary = "Approve Join Request", description = "Allows the owning Sheikh to approve a student's pending join request.")
    @PostMapping("/{circleId}/approve/{userId}")
    public ResponseEntity<ApiResponse<CircleMemberResponse>> approveRequest(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(description = "UUID of the requesting user", required = true) @PathVariable UUID userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CircleMemberResponse member = circleService.approveJoinRequest(circleId, userId, authUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Member approved successfully", member));
    }

    @Operation(summary = "Reject Join Request", description = "Allows the owning Sheikh to reject a student's pending join request.")
    @PostMapping("/{circleId}/reject/{userId}")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(description = "UUID of the requesting user", required = true) @PathVariable UUID userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        circleService.rejectJoinRequest(circleId, userId, authUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Join request rejected"));
    }

    @Operation(summary = "Leave Circle", description = "Allows a user to leave a circle they are currently active in.")
    @PostMapping("/{circleId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveCircle(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        circleService.leaveCircle(circleId, authUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Left circle successfully"));
    }

    @Operation(summary = "Get Circle Active Members", description = "Lists all active members in a circle.")
    @GetMapping("/{circleId}/members")
    public ResponseEntity<ApiResponse<Page<CircleMemberResponse>>> getMembers(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @ParameterObject @PageableDefault(size = 20, sort = "joinedAt") Pageable pageable
    ) {
        Page<CircleMemberResponse> members = circleService.getCircleMembers(circleId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    @Operation(summary = "Remove Member", description = "Allows the owning Sheikh to remove a member from the circle.")
    @DeleteMapping("/{circleId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @Parameter(description = "UUID of the user to remove", required = true) @PathVariable UUID userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        circleService.removeMember(circleId, authUser.getUser(), userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from circle"));
    }

    @Operation(summary = "Get My Active Circles", description = "Retrieve a paginated list of active circles the logged-in user is a member of.")
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<Page<CircleResponse>>> getMyCircles(
            @AuthenticationPrincipal AuthUser authUser,
            @ParameterObject @PageableDefault(size = 20, sort = "startDate") Pageable pageable
    ) {
        Page<CircleResponse> page = circleService.getMyCircles(authUser.getUser(), pageable);
        return ResponseEntity.ok(ApiResponse.success("User circles retrieved successfully", page));
    }

    @Operation(
            summary = "Get an Agora token to join a Circle's audio/video channel",
            description = "Owner or an ACTIVE member only. Circle must not be CANCELLED or COMPLETED."
    )
    @GetMapping("/{circleId}/token")
    public ResponseEntity<ApiResponse<AgoraTokenResponse>> getToken(
            @Parameter(description = "UUID of the circle", required = true) @PathVariable UUID circleId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Token retrieved successfully",
                circleService.getCircleToken(authUser.getUser(), circleId)
        ));
    }
}