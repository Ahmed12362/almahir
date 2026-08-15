package com.almahir.iti.repository;

import com.almahir.iti.model.PaymentTransaction;
import com.almahir.iti.model.SubscriptionPackage;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByPaymobIntentionId(String paymobIntentionId);

    boolean existsByUserAndSubscriptionPackageAndStatus(
            User user, SubscriptionPackage subscriptionPackage, PaymentStatus status
    );

    boolean existsByPaymobTransactionId(String paymobTransactionId);

    Optional<PaymentTransaction> findTopByUserIdAndIdempotencyKeyOrderByCreatedAtDesc(UUID userId, String idempotencyKey);

    long countByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}