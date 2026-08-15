package com.almahir.iti.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private SubscriptionPackage subscriptionPackage;

    private Instant startedAt;

    private Instant expiresAt;

    @Version
    private Long version;
}