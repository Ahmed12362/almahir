package com.almahir.iti.config;

import com.almahir.iti.model.AuthUser;
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

    @Override
    public UserDetails loadUserByUsername(String email) {

        return repository.findByEmail(email)
                .map(AuthUser::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException(email));
    }
}
