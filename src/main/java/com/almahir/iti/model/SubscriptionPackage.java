package com.almahir.iti.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "subscription_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Long priceMinorUnits;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private Integer meetingMinutesAllowed;

    private Integer durationDays;

    @ElementCollection
    @CollectionTable(name = "package_features", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "feature_text")
    @Builder.Default
    private Set<String> features = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;
}