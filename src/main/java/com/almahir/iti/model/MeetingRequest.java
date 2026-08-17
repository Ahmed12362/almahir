package com.almahir.iti.model;

import com.almahir.iti.model.enums.MeetingRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "instant_meeting_requests",
        indexes = {
                @Index(
                        name = "idx_meeting_request_status_expires_at",
                        columnList = "status, expires_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sheikh_id", nullable = false)
    private Sheikh sheikh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingRequestStatus status = MeetingRequestStatus.PENDING;

    @Column(name = "channel_name", nullable = false, unique = true)
    private String channelName;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    @Column(name = "max_duration_minutes")
    private Integer maxDurationMinutes;
    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
    }
}