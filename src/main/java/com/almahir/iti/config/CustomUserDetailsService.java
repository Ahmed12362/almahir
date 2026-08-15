package com.almahir.iti.config;

import com.almahir.iti.model.AuthUser;
import com.almahir.iti.model.AdminAuthUser;
import com.almahir.iti.repository.AdminRepository;
import com.almahir.iti.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository repository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        var user = repository.findByEmail(email);
        if (user.isPresent()) {
            return new AuthUser(user.get());
        }
        var admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return new AdminAuthUser(admin.get());
        }
        throw new UsernameNotFoundException(email);
    }
}
