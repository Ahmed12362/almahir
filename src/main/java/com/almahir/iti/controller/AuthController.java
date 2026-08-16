package com.almahir.iti.controller;

import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Endpoints for user & sheikh registration, login, token refresh, and OAuth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register user or sheikh", description = "Registers a new user or sheikh with multipart data (JSON payload + optional file).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registration successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or payload error")
    })
    @PostMapping(value = {"/user/register", "/sheikh/register"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @RequestPart(value = "data") @Valid RegisterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        UserResponse userResponse = authService.register(request, file,
                uri.contains("user") ? RoleName.STUDENT : RoleName.SHEIKH);


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

    @Operation(summary = "Login user or sheikh", description = "Authenticates credentials and returns JWT tokens.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping({"/user/login", "/sheikh/login", "/admin/login"})
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(request,
                                roleForAuthRequest(uri))
                )
        );
    }

    @Operation(summary = "Google OAuth Login", description = "Authenticates or signs up a user/sheikh using a Google token.")
    @PostMapping({"/user/google", "/sheikh/google"})
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request) {

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        AuthResponse response = authService.loginWithGoogle(request,
                uri.contains("user") ? RoleName.STUDENT : RoleName.SHEIKH);

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

    @Operation(summary = "Refresh JWT Token", description = "Exchanges a valid refresh token for a new access token.")
    @PostMapping({"user/refresh", "sheikh/refresh", "admin/refresh"})
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(

            @Valid
            @RequestBody RefreshTokenRequest request

    ) {

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        AuthResponse response =
                authService.refresh(request,
                        roleForAuthRequest(uri));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfully.",
                        response
                )
        );

    }

    @Operation(summary = "Logout user", description = "Revokes or invalidates the refresh token.")
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

    private RoleName roleForAuthRequest(String uri) {
        if (uri.contains("/admin/")) {
            return RoleName.ADMIN;
        }
        return uri.contains("/user/") ? RoleName.STUDENT : RoleName.SHEIKH;
    }
}
