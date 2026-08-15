package com.almahir.iti.repository;

import com.almahir.iti.model.Admin;
import com.almahir.iti.model.AdminRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshToken, UUID> {
    Optional<AdminRefreshToken> findByToken(String token);
    List<AdminRefreshToken> findByAdminAndRevokedFalse(Admin admin);
}
