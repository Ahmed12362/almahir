package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.CreateSubscriptionPackageRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.exception.AlreadyExists;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.SheikhMapper;
import com.almahir.iti.mapper.UserMapper;
import com.almahir.iti.model.*;
import com.almahir.iti.model.enums.PaymentStatus;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.repository.*;
import com.almahir.iti.repository.spec.PaymentTransactionSpecifications;
import com.almahir.iti.service.AdminService;
import com.almahir.iti.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StudentRepository studentRepository;

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

    @Override
    public PageResponse<PaymentTransactionAdminResponse> getPaymentTransactions(
            PaymentStatus status, UUID userId, Instant from, Instant to, Pageable pageable
    ) {
        Specification<PaymentTransaction> spec =
                PaymentTransactionSpecifications.filter(status, userId, from, to);

        Page<PaymentTransactionAdminResponse> page = paymentTransactionRepository
                .findAll(spec, pageable)
                .map(this::toAdminResponse);

        return PageResponse.from(page);
    }

    @Override
    public PageResponse<StudentAdminResponse> listStudents(Pageable pageable) {
        return PageResponse.from(
                studentRepository.findAll(pageable).map(this::toStudentAdminResponse)
        );
    }

    @Override
    public PageResponse<SheikhResponse> listSheikhs(SheikhStatus status, Pageable pageable) {
        Page<Sheikh> page = (status == null)
                ? sheikhRepository.findAll(pageable)
                : sheikhRepository.findBySheikhStatus(status, pageable);

        return PageResponse.from(page.map(this::toSheikhResponse));
    }

    private StudentAdminResponse toStudentAdminResponse(Student s) {
        User u = s.getUser();
        return new StudentAdminResponse(
                u.getId(), u.getFirstName(), u.getLastName(), u.getUsername(),
                u.getGender(), u.getEmail(), u.getPhoneNumber(),
                u.getProfilePictureUrl(), u.isBlocked()
        );
    }

    private SheikhResponse toSheikhResponse(Sheikh s) {
        User u = s.getUser();
        return new SheikhResponse(
                u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(),
                u.getGender(), u.getEmail(), u.getPhoneNumber(),
                u.getProfilePictureUrl(), s.getSheikhStatus(), s.getRate()
        );
    }

    private PaymentTransactionAdminResponse toAdminResponse(PaymentTransaction tx) {
        User user = tx.getUser();
        SubscriptionPackage pkg = tx.getSubscriptionPackage();

        return new PaymentTransactionAdminResponse(
                tx.getId(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                pkg.getCode(),
                pkg.getName(),
                tx.getMethod().name(),
                tx.getStatus().name(),
                tx.getAmountMinorUnits(),
                tx.getCurrencyCode(),
                tx.getPaymobIntentionId(),
                tx.getPaymobTransactionId(),
                tx.getFailureReasonCode(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
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
