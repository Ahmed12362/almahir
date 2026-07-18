package com.almahir.iti.service;

import com.almahir.iti.model.User;

public interface UserService {
    User getUserByEmail(String email);
}
