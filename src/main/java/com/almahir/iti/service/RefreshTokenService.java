package com.almahir.iti.service;

import com.almahir.iti.model.RefreshToken;
import com.almahir.iti.model.User;

public interface RefreshTokenService {
    RefreshToken create(User user);

    RefreshToken validate(String token);

    void revoke(String token);

    void revokeAll(User user);
}
