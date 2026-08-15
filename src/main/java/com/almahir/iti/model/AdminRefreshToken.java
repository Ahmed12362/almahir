package com.almahir.iti.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class AdminRefreshToken {
    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
