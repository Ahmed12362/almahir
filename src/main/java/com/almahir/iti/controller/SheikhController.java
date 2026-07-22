package com.almahir.iti.controller;

import com.almahir.iti.dto.request.UpdateSheikhRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.SheikhSearchResponse;
import com.almahir.iti.service.SheikhService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sheikh")
@RequiredArgsConstructor
@Tag(name = "Sheikh Management", description = "Endpoints for viewing and managing Sheikh profiles")
public class SheikhController {

    private final SheikhService sheikhService;

    @Operation(summary = "Search Sheikhs", description = "Search registered Sheikhs optionally filtering by name.")
    @GetMapping("/search")
    public ResponseEntity<List<SheikhSearchResponse>> search(
            @Parameter(description = "Optional name query string")
            @RequestParam(required = false, value = "name") String name
    ) {
        return ResponseEntity.ok(sheikhService.search(name));
    }

    @Operation(summary = "Get all Sheikhs", description = "Retrieves a list of all registered Sheikhs.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SheikhResponse>>> getAllSheikhs() {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikhs retrieved successfully",
                sheikhService.getAllSheikhs()
        ));
    }

    @Operation(summary = "Get Sheikh by email", description = "Retrieves details of a Sheikh by their email address.")
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<SheikhResponse>> getSheikhByEmail(
            @Parameter(description = "Sheikh email address")
            @PathVariable String email
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikh retrieved successfully",
                sheikhService.getSheikhByEmail(email)
        ));
    }

    @Operation(summary = "Get Sheikh by username", description = "Retrieves details of a Sheikh by their username.")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<SheikhResponse>> getSheikhByUsername(
            @Parameter(description = "Sheikh username")
            @PathVariable String username
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikh retrieved successfully",
                sheikhService.getSheikhByUsername(username)
        ));
    }

    @Operation(summary = "Get Sheikh by ID", description = "Retrieves details of a Sheikh by their unique ID (UUID).")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SheikhResponse>> getSheikhById(
            @Parameter(description = "Sheikh UUID")
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikh retrieved successfully",
                sheikhService.getSheikhById(id)
        ));
    }

    @Operation(summary = "Update Sheikh profile (JSON)", description = "Updates a Sheikh's profile data (password, name, phoneNumber, profilePictureUrl, sheikhStatus).")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SheikhResponse>> updateSheikhJson(
            @Parameter(description = "Sheikh UUID")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSheikhRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikh profile updated successfully",
                sheikhService.updateSheikh(id, request, null)
        ));
    }

    @Operation(summary = "Update Sheikh profile (Multipart)", description = "Updates a Sheikh's profile data with optional profile picture file upload.")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SheikhResponse>> updateSheikhMultipart(
            @Parameter(description = "Sheikh UUID")
            @PathVariable UUID id,
            @RequestPart(value = "data", required = false) @Valid UpdateSheikhRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sheikh profile updated successfully",
                sheikhService.updateSheikh(id, request, file)
        ));
    }
}
