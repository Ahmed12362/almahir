package com.almahir.iti.controller;

import com.almahir.iti.dto.response.AdminAuthResponse;
import com.almahir.iti.dto.response.AdminStatsResponse;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.service.AdminService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminAuthResponse>> login(@RequestParam @Email String email, @RequestParam @NotBlank String password) {
        return ResponseEntity.ok(ApiResponse.success("Admin login successful", adminService.login(email, password)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam String refreshToken) {
        adminService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Admin logout successful"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sheikhs/{sheikhId}/approve")
    public ResponseEntity<ApiResponse<SheikhResponse>> approve(@PathVariable UUID sheikhId) {
        return ResponseEntity.ok(ApiResponse.success("Sheikh approved", adminService.approveSheikh(sheikhId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sheikhs/{sheikhId}/decline")
    public ResponseEntity<ApiResponse<SheikhResponse>> decline(@PathVariable UUID sheikhId) {
        return ResponseEntity.ok(ApiResponse.success("Sheikh declined", adminService.declineSheikh(sheikhId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<ApiResponse<UserResponse>> block(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User blocked", adminService.blockUser(userId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<ApiResponse<UserResponse>> unblock(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User unblocked", adminService.unblockUser(userId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", adminService.getStats()));
    }
}
