package com.almahir.iti.service;

import com.almahir.iti.dto.request.LoginRequest;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.mapper.UserMapper;
import com.almahir.iti.model.*;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.repository.*;
import com.almahir.iti.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplAdminGateTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock SheikhRepository sheikhRepository;
    @Mock StudentRepository studentRepository;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock GoogleTokenVerifierService googleTokenVerifierService;
    @Mock CloudinaryService cloudinaryService;
    @Mock UserMapper userMapper;

    @InjectMocks AuthServiceImpl authService;

    @Test
    void sheikhLogin_ShouldFailWhenPendingApproval() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("sheikh@example.com")
                .password("encoded")
                .roles(Set.of(Role.builder().name(RoleName.SHEIKH).build()))
                .build();
        Sheikh sheikh = Sheikh.builder()
                .id(user.getId())
                .user(user)
                .sheikhStatus(SheikhStatus.PENDING_APPROVAL)
                .rate(0.0)
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new AuthUser(user));
        when(sheikhRepository.findByIdFetchUser(user.getId())).thenReturn(Optional.of(sheikh));

        assertThrows(ForbiddenOperationException.class,
                () -> authService.login(new LoginRequest("sheikh@example.com", "secret"), RoleName.SHEIKH));
    }
}
