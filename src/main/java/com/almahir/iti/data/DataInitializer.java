package com.almahir.iti.data;

import com.almahir.iti.model.Role;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

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

}
