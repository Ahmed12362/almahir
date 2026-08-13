package com.almahir.iti.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "forgot_password")
public class ForgotPassword {

    @Id
    private String email;

    @Indexed
    private Integer otp;

    @Builder.Default
    private boolean isVerified = false;

    @TimeToLive
    @Builder.Default
    private long timeToLive = 300;
}
