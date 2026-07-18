package com.almahir.iti.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "forgot_password")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer otp;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "expiry_time", nullable = false)
    private Date expiryTime;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
