package com.almahir.iti.data;

import com.almahir.iti.model.Role;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.RoleRepository;
import com.almahir.iti.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Seeds the configured administrator as a normal user with the ADMIN role. */
@Component
@RequiredArgsConstructor
public class AdminUserSeeder {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${admin.seed.username}")
    private String adminUsername;

    @Value("${admin.seed.email}")
    private String adminEmail;

    @Value("${admin.seed.password}")
    private String adminPassword;

    @Transactional
    public void seed() {
        if (userRepository.findByUsername(adminUsername).isPresent()
                || userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role was not initialized"));

        userRepository.save(User.builder()
                .username(adminUsername)
                .firstName("Admin")
                .lastName("User")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .provider("LOCAL")
                .roles(Set.of(adminRole))
                .blocked(false)
                .build());
    }
}
