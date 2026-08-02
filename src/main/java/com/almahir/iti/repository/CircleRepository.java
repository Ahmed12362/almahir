package com.almahir.iti.repository;

import com.almahir.iti.model.Circle;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.CircleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CircleRepository extends JpaRepository<Circle, UUID> {
    Page<Circle> findByTypeAndStatus(CircleType type, CircleStatus status, Pageable pageable);

    Page<Circle> findByType(CircleType type, Pageable pageable);

    Page<Circle> findByOwner(User owner, Pageable pageable);
}