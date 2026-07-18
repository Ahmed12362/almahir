package com.almahir.iti.controller;

import com.almahir.iti.dto.request.ChangePasswordRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.model.User;
import com.almahir.iti.service.ForgotPasswordService;
import com.almahir.iti.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgot-password")
public class ForgotPasswordController {
    private final UserService userService;
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(UserService userService,
                                    ForgotPasswordService forgotPasswordService) {
        this.userService = userService;
        this.forgotPasswordService = forgotPasswordService;
    }

    // Verify email
    @PostMapping("/verify-email/{email}")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);

        forgotPasswordService.generateAndSendOtp(user);

        return ResponseEntity.ok(ApiResponse.success("Email verification successful. OTP sent."));
    }

    // Verify OTP
    @PostMapping("/verify-otp/{otp}/{email}")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@PathVariable Integer otp,
                                                       @PathVariable String email) {
        User user = userService.getUserByEmail(email);

        forgotPasswordService.verifyOtp(otp, user);

        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully."));
    }

    // Reset password
    @PostMapping("/change-password/{email}")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable String email,
                                                           @RequestBody ChangePasswordRequest changePasswordRequest) {
        User user = userService.getUserByEmail(email);

        forgotPasswordService.changePassword(user, changePasswordRequest);

        return ResponseEntity.ok(ApiResponse.success("Password reset successful."));
    }
}
