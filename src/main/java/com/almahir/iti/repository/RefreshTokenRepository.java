package com.almahir.iti.repository;

import com.almahir.iti.model.RefreshToken;
import com.almahir.iti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken , UUID> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUser(User user);
    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
