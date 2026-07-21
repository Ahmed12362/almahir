package com.almahir.iti.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = true)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;
    private String phoneNumber;
    @Column(unique = true)
    private String googleId;
    private String provider;

    @Column(nullable = true)
    private String profilePictureUrl;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH}
            , fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @PrePersist
    void prePersist() {
        if (provider == null || provider.isBlank()) {
            provider = "LOCAL";
        }
    }

    @PreUpdate
    void preUpdate() {
        if (provider == null || provider.isBlank()) {
            provider = "LOCAL";
        }
    }

}
