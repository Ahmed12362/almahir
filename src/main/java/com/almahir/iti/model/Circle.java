package com.almahir.iti.model;

import com.almahir.iti.model.enums.CircleStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "circles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Circle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CircleStatus status = CircleStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sheikh_id", nullable = false)
    private User sheikh;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return this.startDate.isBefore(otherEnd) && this.endDate.isAfter(otherStart);
    }
}