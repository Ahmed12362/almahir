package com.almahir.iti.service;


import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);

    String getEmailFromToken(String token);

    String getTokenType(String token);

    Date getExpiration(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}