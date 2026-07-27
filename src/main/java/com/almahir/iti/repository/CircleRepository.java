package com.almahir.iti.repository;

import com.almahir.iti.model.Circle;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CircleRepository extends JpaRepository<Circle, UUID> {
    Page<Circle> findByStatus(CircleStatus status, Pageable pageable);
    Page<Circle> findBySheikh(User sheikh, Pageable pageable);
}
