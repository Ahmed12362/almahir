package com.almahir.iti.controller;

import com.almahir.iti.dto.request.ChangePasswordRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.model.User;
import com.almahir.iti.service.ForgotPasswordService;
import com.almahir.iti.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgot-password")
@Tag(name = "Forgot Password", description = "Password recovery and OTP operations")
public class ForgotPasswordController {
    private final UserService userService;
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(UserService userService,
                                    ForgotPasswordService forgotPasswordService) {
        this.userService = userService;
        this.forgotPasswordService = forgotPasswordService;
    }

    // Verify email
    @Operation(summary = "Verify Email & Send OTP", description = "Checks if email exists and dispatches a verification OTP.")
    @PostMapping("/verify-email/{email}")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);

        forgotPasswordService.generateAndSendOtp(email);

        return ResponseEntity.ok(ApiResponse.success("Email verification successful. OTP sent."));
    }

    // Verify OTP
    @Operation(summary = "Verify OTP", description = "Validates the supplied OTP against the provided email.")
    @PostMapping("/verify-otp/{otp}/{email}")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@PathVariable Integer otp,
                                                       @PathVariable String email) {
        User user = userService.getUserByEmail(email);

        forgotPasswordService.verifyOtp(otp, email);

        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully."));
    }

    // Reset password
    @Operation(summary = "Change Password", description = "Updates password following successful OTP verification.")
    @PostMapping("/change-password/{email}")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable String email,
                                                            @RequestBody ChangePasswordRequest changePasswordRequest) {
        User user = userService.getUserByEmail(email);

        forgotPasswordService.changePassword(user, changePasswordRequest);

        return ResponseEntity.ok(ApiResponse.success("Password reset successful."));
    }
}
