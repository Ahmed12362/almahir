package com.almahir.iti.controller;

import com.almahir.iti.dto.request.SheikhAvailabilityRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.AuthUser;
import com.almahir.iti.model.enums.MeetingRequestStatus;
import com.almahir.iti.service.InstantMeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instant-meetings")
@RequiredArgsConstructor
@Tag(name = "Instant Meetings", description = """
        Real-time 1-to-1 meeting requests between Students and Sheikhs.
        Connect to STOMP at /ws BEFORE calling createRequest/accept.
        
        TOPICS:
        
        1) /topic/sheikhs/{sheikhId}/requests — Sheikh subscribes to keep their pending list live.
           - SHEIKH_MEETING_REQUEST_RECEIVED: new request came in. Payload: SheikhMeetingRequestEvent object.
           - SHEIKH_MEETING_REQUEST_REMOVED: a request left the pending list (cancelled/expired/superseded). Payload: raw requestId (UUID).
        
        2) /topic/meeting-requests/{requestId} — Student subscribes to track their request; Sheikh too after accepting, to catch MEETING_ENDED.
           - REQUEST_ACCEPTED: payload is AcceptResponse (includes Agora token + channel).
           - REQUEST_DECLINED: payload is a free-text reason string.
           - REQUEST_CANCELLED / REQUEST_EXPIRED / MEETING_ENDED: payload is raw requestId (UUID).
        
        Envelope for every message: { "eventType": "...", "payload": {...} }
        
        NOTE: accepting a request can also auto-decline/auto-cancel the sheikh's or
        student's OTHER pending requests, firing the matching events above on their topics too.
        """)
@SecurityRequirement(name = "bearerAuth")
public class InstantMeetingController {

    private final InstantMeetingService instantMeetingService;

    @Operation(
            summary = "Update Sheikh availability status (AVAILABLE / BUSY / OFFLINE)",
            description = "Sheikh only. No WebSocket subscription needed for this call."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Availability updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized - Valid JWT token required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only registered Sheikhs can update availability", content = @Content)
    })
    @PreAuthorize("hasRole('SHEIKH')")
    @PutMapping("/sheikh/availability")
    public ResponseEntity<ApiResponse<SheikhAvailabilityResponse>> updateAvailability(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SheikhAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Availability updated successfully",
                instantMeetingService.updateSheikhAvailability(authUser.getUser(), request)
        ));
    }

    @Operation(
            summary = "Get a Sheikh's current availability",
            description = "Public/Student use. No WebSocket subscription needed."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Availability retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized - Missing or invalid JWT token", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Sheikh not found", content = @Content)
    })
    @GetMapping("/sheikh/{sheikhId}/availability")
    public ResponseEntity<ApiResponse<SheikhAvailabilityResponse>> getAvailability(
            @Parameter(description = "Sheikh ID") @PathVariable UUID sheikhId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Availability retrieved successfully",
                instantMeetingService.getSheikhAvailability(sheikhId)
        ));
    }

    @Operation(
            summary = "Student sends an instant meeting request to a Sheikh",
            description = """
                    Student only. Sheikh must be AVAILABLE.
                    
                    WEBSOCKET (for the requesting student):
                    Subscribe to /topic/meeting-requests/{requestId} — using the requestId
                    returned in this response — BEFORE or immediately after calling this endpoint.
                    You will receive REQUEST_ACCEPTED, REQUEST_DECLINED, REQUEST_CANCELLED
                    or REQUEST_EXPIRED events on that topic.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Meeting request sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only registered students can request instant meetings", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Sheikh not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Sheikh unavailable, or a pending request already exists", content = @Content)
    })

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/sheikh/{sheikhId}/request")
    public ResponseEntity<ApiResponse<MeetingRequestResponse>> createRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID sheikhId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meeting request sent successfully",
                instantMeetingService.createMeetingRequest(authUser.getUser(), sheikhId)
        ));
    }

    @Operation(
            summary = "Sheikh accepts a pending meeting request",
            description = """
                    Sheikh only. Returns an Agora token + channel for the Sheikh to join immediately.
                    The student receives their own join credentials asynchronously via
                    REQUEST_ACCEPTED on /topic/meeting-requests/{requestId}.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Meeting accepted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only the requested Sheikh can accept this meeting", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meeting request not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Request is not pending, expired, or was modified concurrently", content = @Content)
    })
    @PreAuthorize("hasRole('SHEIKH')")
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<AcceptResponse>> accept(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID requestId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meeting accepted successfully",
                instantMeetingService.acceptMeetingRequest(authUser.getUser(), requestId)
        ));
    }

    @Operation(
            summary = "Sheikh declines a pending meeting request",
            description = "Sheikh only. Fires REQUEST_DECLINED on /topic/meeting-requests/{requestId} for the student."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Meeting request declined successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only the requested Sheikh can decline this meeting", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meeting request not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Request is not in PENDING status", content = @Content)
    })
    @PreAuthorize("hasRole('SHEIKH')")
    @PostMapping("/{requestId}/decline")
    public ResponseEntity<ApiResponse<Void>> decline(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID requestId) {
        instantMeetingService.declineMeetingRequest(authUser.getUser(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Meeting request declined successfully"));
    }

    @Operation(
            summary = "Student cancels their own pending request",
            description = "Student only. Fires REQUEST_CANCELLED on /topic/meeting-requests/{requestId} for the sheikh's UI."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Meeting request cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only the requesting student can cancel this meeting", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meeting request not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Only a pending request can be cancelled", content = @Content)
    })

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID requestId) {
        instantMeetingService.cancelMeetingRequest(authUser.getUser(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Meeting request cancelled successfully"));
    }

    @Operation(
            summary = "Get a fresh Agora token for an already-accepted meeting",
            description = "Sheikh or Student, only if part of the meeting. Use this to reconnect/refresh a token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Token retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "You are not part of this meeting session", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meeting request not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Meeting is not accepted yet", content = @Content)
    })
    @GetMapping("/{requestId}/token")
    public ResponseEntity<ApiResponse<AgoraTokenResponse>> getToken(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID requestId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Token retrieved successfully",
                instantMeetingService.getMeetingToken(authUser.getUser(), requestId)
        ));
    }

    @Operation(
            summary = "End an active meeting",
            description = """
                    Sheikh or Student, only if part of the meeting.
                    Sets the Sheikh back to AVAILABLE and fires MEETING_ENDED on
                    /topic/meeting-requests/{requestId}.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Meeting ended successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "You are not part of this meeting session", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meeting request not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Meeting is not currently active", content = @Content)
    })
    @PostMapping("/{requestId}/end")
    public ResponseEntity<ApiResponse<Void>> end(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID requestId) {
        instantMeetingService.endMeeting(authUser.getUser(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Meeting ended successfully"));
    }

    @Operation(
            summary = "Get pending meeting requests sent to the current Sheikh",
            description = "Sheikh only. Returns all currently PENDING requests so the Sheikh can pick one to accept/decline. Call this when opening the requests screen (the WebSocket topic handles live updates while the screen is open)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Pending requests retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only registered Sheikhs can view pending requests", content = @Content)
    })
    @PreAuthorize("hasRole('SHEIKH')")
    @GetMapping("/sheikh/pending")
    public ResponseEntity<ApiResponse<PageResponse<PendingMeetingRequestResponse>>> getPendingRequests(
            @AuthenticationPrincipal AuthUser authUser,
            @ParameterObject @PageableDefault(size = 10, sort = "requestedAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pending requests retrieved successfully",
                instantMeetingService.getPendingRequests(authUser.getUser(), pageable)
        ));
    }

    @Operation(
            summary = "Get the current student's completed meeting history",
            description = "Student only. Returns meetings that have ended (ENDED status), most recent first."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Meeting history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Only registered students can view meeting history", content = @Content)
    })
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/history")
    public ResponseEntity<ApiResponse<PageResponse<StudentMeetingHistoryResponse>>> getStudentHistory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) List<MeetingRequestStatus> status,
            @ParameterObject @PageableDefault(size = 10, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meeting history retrieved successfully",
                instantMeetingService.getStudentMeetingHistory(authUser.getUser(), status, pageable)
        ));
    }

    @PreAuthorize("hasRole('SHEIKH')")
    @GetMapping("/sheikh/history")
    public ResponseEntity<ApiResponse<PageResponse<SheikhMeetingHistoryResponse>>> getSheikhHistory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) List<MeetingRequestStatus> status,
            @ParameterObject @PageableDefault(size = 10, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Meeting history retrieved successfully",
                instantMeetingService.getSheikhMeetingHistory(authUser.getUser(), status, pageable)
        ));
    }
}