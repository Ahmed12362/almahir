package com.almahir.iti.repository;

import com.almahir.iti.model.Role;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    long countByRoles_Name(RoleName roleName);

    @Query("select count(u) from User u where u.blocked = true")
    long countBlockedUsers();
}
