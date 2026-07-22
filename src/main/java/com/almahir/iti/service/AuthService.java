package com.almahir.iti.service;

import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.enums.RoleName;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

//    AuthResponse login(LoginRequest request);

    AuthResponse login(LoginRequest request, RoleName requiredRole);

    AuthResponse loginWithGoogle(GoogleAuthRequest request, RoleName requiredRole);

//    UserResponse register(RegisterRequest request, MultipartFile file);

    UserResponse register(RegisterRequest request, MultipartFile file, RoleName role);

    AuthResponse refresh(RefreshTokenRequest request, RoleName roleName);

    void logout(RefreshTokenRequest request);
}
