package com.almahir.iti.service;

import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.User;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleAuthRequest request);

    UserResponse register(RegisterRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
