package com.almahir.iti.repository;

import com.almahir.iti.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken , UUID> {
}
