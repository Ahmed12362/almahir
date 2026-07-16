package com.almahir.iti.service.impl;

import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.service.AuthService;
import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.exception.AlreadyExists;
import com.almahir.iti.model.AuthUser;
import com.almahir.iti.model.RefreshToken;
import com.almahir.iti.model.Role;
import com.almahir.iti.model.RoleName;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.RoleRepository;
import com.almahir.iti.repository.UserRepository;
import com.almahir.iti.service.JwtService;
import com.almahir.iti.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse login(LoginRequest request) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        String accessToken =
                jwtService.generateAccessToken(authUser);

        RefreshToken refreshToken =
                refreshTokenService.create(authUser.getUser());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    public UserResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AlreadyExists(request.email());
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() ->
                        new ResourceNotFound(RoleName.USER.toString()));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .build();


        //        AuthUser authUser = new AuthUser(savedUser);
//
//        String accessToken =
//                jwtService.generateAccessToken(authUser);
//
//        RefreshToken refreshToken =
//                refreshTokenService.create(savedUser);

        User savedUser = userRepository.save(user);
        return new UserResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber());
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.validate(
                        request.refreshToken()
                );

        AuthUser authUser =
                new AuthUser(refreshToken.getUser());

        String accessToken =
                jwtService.generateAccessToken(authUser);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    public void logout(RefreshTokenRequest request) {

        refreshTokenService.revoke(
                request.refreshToken()
        );

    }
}
