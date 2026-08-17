package com.almahir.iti.service;

import com.almahir.iti.dto.request.CreateSubscriptionPackageRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.enums.PaymentStatus;
import com.almahir.iti.model.enums.SheikhStatus;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AdminService {
    SheikhResponse approveSheikh(UUID sheikhId);

    SheikhResponse declineSheikh(UUID sheikhId);

    UserResponse blockUser(UUID userId);

    UserResponse unblockUser(UUID userId);

    SubscriptionPackageResponse createSubscriptionPackage(CreateSubscriptionPackageRequest request);

    PageResponse<PaymentTransactionAdminResponse> getPaymentTransactions(
            PaymentStatus status, UUID userId, Instant from, Instant to, Pageable pageable
    );
}
