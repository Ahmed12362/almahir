package com.almahir.iti.controller;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.CircleMemberResponse;
import com.almahir.iti.dto.response.CircleResponse;
import com.almahir.iti.dto.response.PagedApiResponse;
import com.almahir.iti.model.PageMeta;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.service.CircleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/circles")
public class CircleController {
    private final CircleService circleService;

    public CircleController(CircleService circleService) {
        this.circleService = circleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CircleResponse>> createCircle(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CircleCreateRequest request
    ) {
        CircleResponse circle = circleService.createCircle(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Circle created successfully.", circle));
    }

    @GetMapping
    public ResponseEntity<PagedApiResponse<List<CircleResponse>>> listCircles(
            @RequestParam(required = false) CircleStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit);
        Page<CircleResponse> result = circleService.listCircles(status, pageable);
        PageMeta meta = new PageMeta(page, limit, result.getTotalElements());
        return ResponseEntity.ok(PagedApiResponse.success("Circles retrieved successfully.", result.getContent(), meta));
    }

    @GetMapping("/{circleId}")
    public ResponseEntity<ApiResponse<CircleResponse>> getCircle(@PathVariable UUID circleId) {
        return ResponseEntity.ok(ApiResponse.success("Circle retrieved successfully.", circleService.getCircle(circleId)));
    }

    @PatchMapping("/{circleId}")
    public ResponseEntity<ApiResponse<CircleResponse>> updateCircle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID circleId,
            @RequestBody CircleUpdateRequest request
    ) {
        CircleResponse circle = circleService.updateCircle(currentUser, circleId, request);
        return ResponseEntity.ok(ApiResponse.success("Circle updated successfully.", circle));
    }

    @DeleteMapping("/{circleId}")
    public ResponseEntity<ApiResponse<Void>> cancelCircle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID circleId
    ) {
        circleService.cancelCircle(currentUser, circleId);
        return ResponseEntity.ok(ApiResponse.success("Circle cancelled successfully."));
    }

    @PostMapping("/{circleId}/join")
    public ResponseEntity<ApiResponse<CircleResponse>> joinCircle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID circleId
    ) {
        CircleResponse circle = circleService.joinCircle(currentUser, circleId);
        return ResponseEntity.ok(ApiResponse.success("You have joined the Circle successfully.", circle));
    }

    @PostMapping("/{circleId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveCircle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID circleId
    ) {
        circleService.leaveCircle(currentUser, circleId);
        return ResponseEntity.ok(ApiResponse.success("You have left the Circle."));
    }

    @GetMapping("/{circleId}/members")
    public ResponseEntity<ApiResponse<List<CircleMemberResponse>>> listMembers(@PathVariable UUID circleId) {
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully.", circleService.listMembers(circleId)));
    }

    @DeleteMapping("/{circleId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID circleId,
            @PathVariable UUID userId
    ) {
        circleService.removeMember(currentUser, circleId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from Circle successfully."));
    }

    @GetMapping("/users/me/circles")
    public ResponseEntity<ApiResponse<List<CircleResponse>>> myCircles(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) CircleStatus status
    ) {
        List<CircleResponse> circles = circleService.listMyCircles(currentUser, status);
        return ResponseEntity.ok(ApiResponse.success("Circles retrieved successfully.", circles));
    }
}