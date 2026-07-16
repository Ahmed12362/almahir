package com.almahir.iti.service.impl;

import com.almahir.iti.model.AuthUser;
import com.almahir.iti.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.key}")
    private String secretKey;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        long now = System.currentTimeMillis();
//        AuthUser authUser = (AuthUser) userDetails;
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTokenExpiration))
                .signWith(getKey())
                .claim("roles", roles)
//                .claim("userId", authUser.getUser().getId())
                .claim("type", "access")
                .compact();
    }

    Key getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Override
    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public String getTokenType(String token) {
        return extractAllClaims(token)
                .get("type", String.class);
    }

    @Override
    public Date getExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {

        if (isExpired(token))
            return false;
        if (!"access".equals(getTokenType(token)))
            return false;
        return getEmailFromToken(token)
                .equals(userDetails.getUsername());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private boolean isExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }
}
