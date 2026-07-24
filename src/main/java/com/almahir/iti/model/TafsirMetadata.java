package com.almahir.iti.model;

import com.almahir.iti.model.enums.TafsirBuildStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tafsir_lang", columnNames = {"tafsir_key", "language"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TafsirMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tafsir_key", nullable = false, length = 50)
    private String tafsirKey;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "language_name", nullable = false, length = 50)
    private String languageName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TafsirBuildStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}