package com.almahir.iti.service.impl;

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
import com.almahir.iti.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final SheikhRepository sheikhRepository;
    private final SheikhMapper sheikhMapper;
    private final CircleRepository circleRepository;

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
        sheikh.setSheikhStatus(SheikhStatus.Declined);
        sheikh.getUser().setBlocked(true);
        sheikhRepository.save(sheikh);
        return sheikhMapper.toSheikhResponse(sheikh);
    }

    @Override
    @Transactional
    public UserResponse blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setBlocked(true);
        User blockedUser = userRepository.save(user);
        // Invalidate every session, including sessions opened on other devices.
        refreshTokenService.revokeAll(blockedUser);
        return userMapper.toUserResponse(blockedUser);
    }

    @Override
    @Transactional
    public UserResponse unblockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setBlocked(false);
        return userMapper.toUserResponse(userRepository.save(user));
    }
}
