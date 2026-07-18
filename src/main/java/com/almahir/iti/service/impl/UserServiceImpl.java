package com.almahir.iti.service.impl;

import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.UserRepository;
import com.almahir.iti.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User"));
    }
}
