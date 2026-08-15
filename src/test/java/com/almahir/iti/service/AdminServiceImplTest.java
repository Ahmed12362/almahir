package com.almahir.iti.service;

import com.almahir.iti.dto.response.AdminAuthResponse;
import com.almahir.iti.dto.response.AdminStatsResponse;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.mapper.SheikhMapper;
import com.almahir.iti.mapper.UserMapper;
import com.almahir.iti.model.*;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.Gender;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.repository.*;
import com.almahir.iti.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock AdminRepository adminRepository;
    @Mock AdminRefreshTokenRepository adminRefreshTokenRepository;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock SheikhRepository sheikhRepository;
    @Mock SheikhMapper sheikhMapper;
    @Mock CircleRepository circleRepository;

    @InjectMocks AdminServiceImpl adminService;

    private Admin admin;
    private User user;
    private Sheikh sheikh;
    private Circle circle;

    @BeforeEach
    void setUp() {
        admin = Admin.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@almahir.jets")
                .password("encoded")
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .username("student1")
                .firstName("Ali")
                .lastName("Ahmed")
                .gender(Gender.MALE)
                .email("student@example.com")
                .roles(Set.of(Role.builder().name(RoleName.STUDENT).build()))
                .build();

        sheikh = Sheikh.builder()
                .id(UUID.randomUUID())
                .user(User.builder()
                        .id(UUID.randomUUID())
                        .username("sheikh1")
                        .email("sheikh@example.com")
                        .roles(Set.of(Role.builder().name(RoleName.SHEIKH).build()))
                        .build())
                .sheikhStatus(SheikhStatus.PENDING_APPROVAL)
                .rate(0.0)
                .build();

        circle = Circle.builder()
                .id(UUID.randomUUID())
                .title("Circle 1")
                .type(com.almahir.iti.model.enums.CircleType.PUBLIC)
                .status(CircleStatus.ONGOING)
                .owner(sheikh.getUser())
                .build();
    }

    @Test
    void login_ShouldReturnTokens() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new AdminAuthUser(admin));
        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(adminRefreshTokenRepository.save(any(AdminRefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminAuthResponse response = adminService.login("admin@almahir.jets", "secret");

        assertEquals("access", response.accessToken());
        assertNotNull(response.refreshToken());
    }

    @Test
    void approveAndBlockFlow_ShouldWork() {
        UserResponse userResponse = new UserResponse(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getGender(), user.getEmail(), user.getPhoneNumber(), user.getProfilePictureUrl(), user.getProvider(), Set.of("STUDENT"));
        SheikhResponse sheikhResponse = new SheikhResponse(sheikh.getId(), sheikh.getUser().getUsername(), sheikh.getUser().getFirstName(), sheikh.getUser().getLastName(), sheikh.getUser().getGender(), sheikh.getUser().getEmail(), sheikh.getUser().getPhoneNumber(), sheikh.getUser().getProfilePictureUrl(), SheikhStatus.AVAILABLE, 0.0);

        when(sheikhRepository.findByIdFetchUser(sheikh.getId())).thenReturn(Optional.of(sheikh));
        when(sheikhMapper.toSheikhResponse(any(Sheikh.class))).thenReturn(sheikhResponse);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        when(circleRepository.count()).thenReturn(1L);
        when(circleRepository.countByStatus(CircleStatus.ONGOING)).thenReturn(1L);
        when(userRepository.countByRoles_Name(RoleName.STUDENT)).thenReturn(1L);
        when(userRepository.countByRoles_Name(RoleName.SHEIKH)).thenReturn(1L);
        when(userRepository.countBlockedUsers()).thenReturn(0L);
        when(circleRepository.findAll()).thenReturn(List.of(circle));

        SheikhResponse approved = adminService.approveSheikh(sheikh.getId());
        UserResponse blocked = adminService.blockUser(user.getId());
        AdminStatsResponse stats = adminService.getStats();

        assertEquals(SheikhStatus.AVAILABLE, approved.sheikhStatus());
        assertTrue(blocked.roles().contains("STUDENT"));
        assertEquals(1L, stats.totalCircles());
        assertEquals(1L, stats.runningCircles());
        assertEquals(1L, stats.studentsCount());
        assertEquals(1L, stats.sheikhsCount());
    }
}
