package com.almahir.iti.controller;

import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin("*")
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @RequestPart("data") @Valid RegisterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        UserResponse userResponse = authService.register(request, file);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/users/{id}")
                .buildAndExpand(userResponse.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse<>(
                        true,
                        "Registration successful",
                        userResponse,
                        Instant.now()
                ));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(request)
                )
        );
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request) {

        AuthResponse response = authService.loginWithGoogle(request);

        HttpStatus status = response.isNewUser()
                ? HttpStatus.CREATED
                : HttpStatus.OK;

        String message = response.isNewUser()
                ? "New user account created and authenticated via Google."
                : "Google authentication successful.";

        return ResponseEntity.status(status)
                .body(ApiResponse.success(
                                message,
                                response
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(

            @Valid
            @RequestBody RefreshTokenRequest request

    ) {

        AuthResponse response =
                authService.refresh(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfully.",
                        response
                )
        );

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(

            @Valid
            @RequestBody RefreshTokenRequest request

    ) {

        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successful."
                )
        );

    }
}
