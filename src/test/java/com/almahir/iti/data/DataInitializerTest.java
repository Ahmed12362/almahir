package com.almahir.iti.data;

import com.almahir.iti.model.Admin;
import com.almahir.iti.model.Role;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.AdminRepository;
import com.almahir.iti.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Test
    void initAdmin_ShouldSeedOnlyWhenMissing() throws Exception {
        RoleRepository roleRepository = mock(RoleRepository.class);
        AdminRepository adminRepository = mock(AdminRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(adminRepository.findByEmail("admin@almahir.jets")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin")).thenReturn("encoded-admin");

        DataInitializer initializer = new DataInitializer(roleRepository, adminRepository, passwordEncoder);
        setField(initializer, "adminUsername", "admin");
        setField(initializer, "adminEmail", "admin@almahir.jets");
        setField(initializer, "adminPassword", "admin");

        initializer.initAdmin().run();

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("admin@almahir.jets", captor.getValue().getEmail());
        assertEquals("encoded-admin", captor.getValue().getPassword());
    }

    @Test
    void initAdmin_ShouldNotDuplicateExistingAdmin() throws Exception {
        RoleRepository roleRepository = mock(RoleRepository.class);
        AdminRepository adminRepository = mock(AdminRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(Admin.builder().build()));

        DataInitializer initializer = new DataInitializer(roleRepository, adminRepository, passwordEncoder);
        setField(initializer, "adminUsername", "admin");
        setField(initializer, "adminEmail", "admin@almahir.jets");
        setField(initializer, "adminPassword", "admin");

        initializer.initAdmin().run();

        verify(adminRepository, never()).save(any());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
