package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.CreateSubscriptionPackageRequest;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.SubscriptionPackageResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.exception.AlreadyExists;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
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
    private final SubscriptionPackageRepository subscriptionPackageRepository;

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
        sheikh.setSheikhStatus(SheikhStatus.DECLINED);
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

    @Override
    @Transactional
    public SubscriptionPackageResponse createSubscriptionPackage(CreateSubscriptionPackageRequest request) {
        String code = request.code().trim();
        if (subscriptionPackageRepository.existsByCode(code)) {
            throw new AlreadyExists("Subscription package code '" + code + "'");
        }

        SubscriptionPackage subscriptionPackage = SubscriptionPackage.builder()
                .code(code)
                .name(request.name().trim())
                .description(request.description())
                .priceMinorUnits(request.priceMinorUnits())
                .currencyCode(request.currencyCode().trim().toUpperCase(Locale.ROOT))
                .meetingMinutesAllowed(request.meetingMinutesAllowed())
                .durationDays(request.durationDays())
                .features(request.features() == null ? new HashSet<>() : new HashSet<>(request.features()))
                .active(request.active() == null || request.active())
                .build();

        SubscriptionPackage savedPackage = subscriptionPackageRepository.save(subscriptionPackage);
        return toSubscriptionPackageResponse(savedPackage);
    }

    private SubscriptionPackageResponse toSubscriptionPackageResponse(SubscriptionPackage subscriptionPackage) {
        return new SubscriptionPackageResponse(
                subscriptionPackage.getId(),
                subscriptionPackage.getCode(),
                subscriptionPackage.getName(),
                subscriptionPackage.getDescription(),
                subscriptionPackage.getPriceMinorUnits(),
                subscriptionPackage.getCurrencyCode(),
                subscriptionPackage.getMeetingMinutesAllowed(),
                subscriptionPackage.getDurationDays(),
                Set.copyOf(subscriptionPackage.getFeatures()),
                subscriptionPackage.isActive()
        );
    }
}
