package com.almahir.iti.controller;

import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.RoleName;
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


    @PostMapping({"/user/login", "/sheikh/login"})
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(request,
                                uri.contains("user") ? RoleName.STUDENT : RoleName.SHEIKH)
                )
        );
    }

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

    @PostMapping({"user/refresh", "sheikh/refresh"})
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(

            @Valid
            @RequestBody RefreshTokenRequest request

    ) {

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        AuthResponse response =
                authService.refresh(request,
                        uri.contains("user") ? RoleName.STUDENT : RoleName.SHEIKH);

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
