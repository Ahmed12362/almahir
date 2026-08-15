package com.almahir.iti.repository;

import com.almahir.iti.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    Optional<UserSubscription> findByUserId(UUID userId);
}