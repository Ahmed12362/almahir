package com.almahir.iti.service;

import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleAuthRequest request);

    UserResponse register(RegisterRequest request, MultipartFile file);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
