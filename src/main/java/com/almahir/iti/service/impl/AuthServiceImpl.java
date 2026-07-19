package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.GoogleAuthRequest;
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
import com.almahir.iti.service.GoogleTokenVerifierService;
import com.almahir.iti.service.JwtService;
import com.almahir.iti.service.RefreshTokenService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

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
                refreshToken.getToken(),
                false,
                toUserResponse(authUser.getUser())
        );
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {

        GoogleIdToken.Payload payload =
                googleTokenVerifierService.verify(request.idToken());

        String googleId = payload.getSubject();
        String email = payload.getEmail().toLowerCase();
        String fullName = (String) payload.get("name");
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");

        AtomicBoolean isNewUser = new AtomicBoolean(true);

        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> {
                    if (existingUser.getGoogleId() == null || existingUser.getGoogleId().isBlank()) {
                        existingUser.setGoogleId(googleId);
                    }
                    if (existingUser.getProvider() == null || existingUser.getProvider().isBlank()) {
                        existingUser.setProvider("GOOGLE");
                    }
                    isNewUser.set(false);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> createGoogleUser(email, googleId, fullName, firstName, lastName));


        AuthUser authUser = new AuthUser(user);

        String accessToken =
                jwtService.generateAccessToken(authUser);

        RefreshToken refreshToken =
                refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                isNewUser.get(),
                toUserResponse(user)
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
                .provider("LOCAL")
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
        return toUserResponse(savedUser);
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
                refreshToken.getToken(),
                false,
                toUserResponse(refreshToken.getUser())
        );
    }

    @Override
    public void logout(RefreshTokenRequest request) {

        refreshTokenService.revoke(
                request.refreshToken()
        );

    }

    private User createGoogleUser(
            String email,
            String googleId,
            String fullName,
            String firstName,
            String lastName
    ) {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() ->
                        new ResourceNotFound(RoleName.USER.toString()));

        String resolvedFirstName = hasText(firstName) ? firstName : resolveFirstName(fullName, email);
        String resolvedLastName = hasText(lastName) ? lastName : resolveLastName(fullName);

        User user = User.builder()
                .username(email)
                .firstName(resolvedFirstName)
                .lastName(resolvedLastName)
                .email(email)
                .googleId(googleId)
                .provider("GOOGLE")
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProvider(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveFirstName(String fullName, String email) {
        if (!hasText(fullName)) {
            return email.substring(0, email.indexOf("@"));
        }
        return fullName.trim().split("\\s+", 2)[0];
    }

    private String resolveLastName(String fullName) {
        if (!hasText(fullName)) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
