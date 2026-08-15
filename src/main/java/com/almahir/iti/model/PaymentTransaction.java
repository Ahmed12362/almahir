package com.almahir.iti.model;

import com.almahir.iti.model.enums.PaymentMethod;
import com.almahir.iti.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private SubscriptionPackage subscriptionPackage;

    @Column(nullable = false, unique = true)
    private String paymobIntentionId;

    @Column(unique = true)
    private String paymobTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Long amountMinorUnits;

    @Column(nullable = false)
    private String currencyCode;

    private String failureReasonCode;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(length = 500)
    private String paymobClientSecret;

    private Instant intentionExpiresAt;
    @Column(nullable = false)
    private String idempotencyKey;

    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}