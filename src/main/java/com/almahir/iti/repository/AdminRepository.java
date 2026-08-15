package com.almahir.iti.repository;

import com.almahir.iti.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUsername(String username);
}
