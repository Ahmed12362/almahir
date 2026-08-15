package com.almahir.iti.service.impl;

import com.almahir.iti.dto.response.AdminAuthResponse;
import com.almahir.iti.dto.response.AdminStatsResponse;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.SheikhMapper;
import com.almahir.iti.mapper.UserMapper;
import com.almahir.iti.model.*;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.repository.*;
import com.almahir.iti.service.AdminService;
import com.almahir.iti.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;
    private final AdminRefreshTokenRepository adminRefreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SheikhRepository sheikhRepository;
    private final SheikhMapper sheikhMapper;
    private final CircleRepository circleRepository;

    @Override
    public AdminAuthResponse login(String email, String password) {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        AdminAuthUser principal = (AdminAuthUser) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = UUID.randomUUID().toString();
        adminRefreshTokenRepository.save(AdminRefreshToken.builder()
                .admin(principal.getAdmin())
                .token(refreshToken)
                .expiresAt(java.time.Instant.now().plusMillis(604800000L))
                .revoked(false)
                .build());
        return new AdminAuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        AdminRefreshToken token = adminRefreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Admin refresh token not found"));
        token.setRevoked(true);
        adminRefreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public SheikhResponse approveSheikh(UUID sheikhId) {
        Sheikh sheikh = sheikhRepository.findByIdFetchUser(sheikhId)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with id: " + sheikhId));
        sheikh.setSheikhStatus(SheikhStatus.AVAILABLE);
        sheikhRepository.save(sheikh);
        return sheikhMapper.toSheikhResponse(sheikh);
    }

    @Override
    @Transactional
    public SheikhResponse declineSheikh(UUID sheikhId) {
        Sheikh sheikh = sheikhRepository.findByIdFetchUser(sheikhId)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with id: " + sheikhId));
        sheikh.setSheikhStatus(SheikhStatus.OFFLINE);
        sheikhRepository.save(sheikh);
        return sheikhMapper.toSheikhResponse(sheikh);
    }

    @Override
    @Transactional
    public UserResponse blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setBlocked(true);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse unblockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setBlocked(false);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public AdminStatsResponse getStats() {
        long studentsCount = userRepository.countByRoles_Name(RoleName.STUDENT);
        long sheikhsCount = userRepository.countByRoles_Name(RoleName.SHEIKH);
        long blockedUsersCount = userRepository.countBlockedUsers();
        long totalCircles = circleRepository.count();
        long runningCircles = circleRepository.countByStatus(CircleStatus.ONGOING);
        Map<UUID, Long> circlesPerSheikhId = circleRepository.findAll().stream()
                .filter(c -> c.getOwner() != null)
                .collect(Collectors.groupingBy(c -> c.getOwner().getId(), Collectors.counting()));
        Map<String, Long> circlesPerSheikhEmail = circleRepository.findAll().stream()
                .filter(c -> c.getOwner() != null)
                .collect(Collectors.groupingBy(c -> c.getOwner().getEmail(), Collectors.counting()));
        return new AdminStatsResponse(studentsCount, sheikhsCount, blockedUsersCount, totalCircles, runningCircles, circlesPerSheikhId, circlesPerSheikhEmail);
    }
}
