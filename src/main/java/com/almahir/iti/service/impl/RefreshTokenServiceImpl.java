package com.almahir.iti.service.impl;

import com.almahir.iti.exception.InvalidRefreshTokenException;
import com.almahir.iti.model.RefreshToken;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.RefreshTokenRepository;
import com.almahir.iti.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Override
    public RefreshToken create(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Invalid refresh token"));
        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token revoked");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
        if (refreshToken.getUser().isBlocked()) {
            throw new InvalidRefreshTokenException("User is blocked");
        }
        return refreshToken;
    }
    @Transactional
    @Override
    public void revoke(String token) {
        RefreshToken refreshToken = validate(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
    @Transactional
    @Override
    public void revokeAll(User user) {
        List<RefreshToken> tokens =
                refreshTokenRepository.findByUserAndRevokedFalse(user);
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }
}
