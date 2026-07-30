package com.almahir.iti.model;

import com.almahir.iti.model.enums.SheikhStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sheikh {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private SheikhStatus sheikhStatus;

    private Double rate = 0.0;
    @Version
    private Long version;
}