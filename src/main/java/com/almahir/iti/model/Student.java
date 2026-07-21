package com.almahir.iti.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    private UUID id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
