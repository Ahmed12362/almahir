package com.almahir.iti.data;

import com.almahir.iti.model.Admin;
import com.almahir.iti.model.Role;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.AdminRepository;
import com.almahir.iti.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.username}")
    private String adminUsername;

    @Value("${admin.seed.email}")
    private String adminEmail;

    @Value("${admin.seed.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initRoles() {
        return args -> {

            for (RoleName roleName : RoleName.values()) {
                roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName(roleName);
                            return roleRepository.save(role);
                });
            }
        };
    }

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            if (adminRepository.findByUsername(adminUsername).isEmpty()
                    && adminRepository.findByEmail(adminEmail).isEmpty()) {
                adminRepository.save(Admin.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .blocked(false)
                        .build());
            }
        };
    }

}
