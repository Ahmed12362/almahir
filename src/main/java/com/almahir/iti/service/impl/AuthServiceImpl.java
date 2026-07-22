package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.GoogleAuthRequest;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.exception.InvalidUserRoleException;
import com.almahir.iti.exception.ImageUploadException;
import com.almahir.iti.exception.RegistrationFailedException;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.mapper.UserMapper;
import com.almahir.iti.service.*;
import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.dto.request.RefreshTokenRequest;
import com.almahir.iti.dto.request.RegisterRequest;
import com.almahir.iti.dto.response.AuthResponse;
import com.almahir.iti.exception.AlreadyExists;
import com.almahir.iti.model.AuthUser;
import com.almahir.iti.model.RefreshToken;
import com.almahir.iti.model.Role;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.model.Student;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.RoleRepository;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.StudentRepository;
import com.almahir.iti.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.UncheckedIllegalAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SheikhRepository sheikhRepository;
    private final StudentRepository studentRepository;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    private final CloudinaryService cloudinaryService;

    private final UserMapper userMapper;

//    @Override
//    public AuthResponse login(LoginRequest request) {
//
//        return login(request, null);
//    }

    @Override
    public AuthResponse login(LoginRequest request, RoleName requiredRole) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        if (requiredRole != null && !hasRole(authUser.getUser(), requiredRole)) {
            throw new BadCredentialsException("Authentication failed.");
        }

        String accessToken =
                jwtService.generateAccessToken(authUser);

        RefreshToken refreshToken =
                refreshTokenService.create(authUser.getUser());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                false,
                userMapper.toUserResponse(authUser.getUser())
        );
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleAuthRequest request, RoleName requiredRole) {

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
                .orElseGet(() -> createGoogleUser(email, googleId, fullName, firstName, lastName, requiredRole));

        if(user.getRoles()
                .stream()
                .noneMatch(role ->
                        role.getName()
                                .toString()
                                .equals(
                                        requiredRole.toString())
                )
        ) {
            throw new InvalidUserRoleException("You are " + user.getRoles().stream().findFirst().get().getName().toString() + ", and that app is for " + requiredRole.toString() + " Only");
        }


        AuthUser authUser = new AuthUser(user);

        String accessToken =
                jwtService.generateAccessToken(authUser);

        RefreshToken refreshToken =
                refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                isNewUser.get(),
                userMapper.toUserResponse(user)
        );
    }

//    @Override
//    @Transactional
//    public UserResponse register(RegisterRequest request, MultipartFile file) {
//
//        return register(request, file, RoleName.USER);
//    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request, MultipartFile file, RoleName roleName) {
        try {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new AlreadyExists(request.email());
            }

            validateRegistrationRole(roleName);

            Role userRole = roleRepository.findByName(roleName)
                    .orElseThrow(() ->
                            new ResourceNotFound("Role Not Found: " + roleName));

            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                imageUrl = cloudinaryService.uploadFile(file, "almahir/profile_pictures");
            }

            User user = User.builder()
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .username(request.username())
                    .email(request.email())
                    .phoneNumber(request.phoneNumber())
                    .password(passwordEncoder.encode(request.password()))
                    .profilePictureUrl(imageUrl)
                    .provider("LOCAL")
                    .roles(Set.of(userRole))
                    .build();

            User savedUser = userRepository.save(user);
            createProfile(savedUser, roleName);
            return userMapper.toUserResponse(savedUser);
        } catch (AlreadyExists | ResourceNotFound | ImageUploadException | InvalidUserRoleException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyExists(request.email());
        } catch (Exception ex) {
            log.error("Registration failed for role {}", roleName, ex);
            throw new RegistrationFailedException();
        }
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request, RoleName roleName) {

        RefreshToken refreshToken =
                refreshTokenService.validate(
                        request.refreshToken()
                );

        if(refreshToken.getUser()
                .getRoles()
                .stream()
                .noneMatch(role ->
                        role.getName()
                                .toString()
                                .equals(roleName.toString())
                )
        )
        {
            throw new InvalidUserRoleException("You are " + refreshToken.getUser().getRoles().stream().findFirst().get().getName().toString() + ", and that app is for " + roleName.toString() + " Only");
        }

        AuthUser authUser =
                new AuthUser(refreshToken.getUser());

        String accessToken =
                jwtService.generateAccessToken(authUser);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                false,
                userMapper.toUserResponse(refreshToken.getUser())
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
            String lastName,
            RoleName requiredRole
    ) {
        Role userRole = roleRepository.findByName(requiredRole)
                .orElseThrow(() ->
                        new ResourceNotFound("Role Not Found:" + requiredRole.toString()));

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

        User savedUser = userRepository.save(user);
        createProfile(savedUser, requiredRole);
        return savedUser;
    }

    private void createProfile(User user, RoleName roleName) {
        if (roleName == RoleName.SHEIKH) {
            sheikhRepository.save(Sheikh.builder()
                    .user(user)
                    .sheikhStatus(SheikhStatus.AVAILABLE)
                    .rate(0.0)
                    .build());
        }

        if (roleName == RoleName.STUDENT) {
            studentRepository.save(Student.builder()
                    .user(user)
                    .build());
        }
    }

    private void validateRegistrationRole(RoleName roleName) {
        if (roleName != RoleName.SHEIKH
                && roleName != RoleName.STUDENT) {
            throw new IllegalArgumentException("Unsupported registration role: " + roleName);
        }
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
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
