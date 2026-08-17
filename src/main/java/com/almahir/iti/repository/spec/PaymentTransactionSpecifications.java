package com.almahir.iti.repository.spec;

import com.almahir.iti.model.PaymentTransaction;
import com.almahir.iti.model.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class PaymentTransactionSpecifications {

    private PaymentTransactionSpecifications() {
    }

    public static Specification<PaymentTransaction> hasStatus(PaymentStatus status) {
        return (root, query, cb) -> status == null
                ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<PaymentTransaction> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? null
                : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<PaymentTransaction> createdFrom(Instant from) {
        return (root, query, cb) -> from == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<PaymentTransaction> createdTo(Instant to) {
        return (root, query, cb) -> to == null
                ? null
                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<PaymentTransaction> filter(
            PaymentStatus status, UUID userId, Instant from, Instant to
    ) {
        return Specification.where(hasStatus(status))
                .and(hasUserId(userId))
                .and(createdFrom(from))
                .and(createdTo(to));
    }
}