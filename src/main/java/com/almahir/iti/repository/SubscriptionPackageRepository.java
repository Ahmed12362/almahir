package com.almahir.iti.repository;

import com.almahir.iti.dto.response.SubscriptionPackageResponse;
import com.almahir.iti.model.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, UUID> {
    Optional<SubscriptionPackage> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);

    List<SubscriptionPackage> findByActiveTrue();
}
