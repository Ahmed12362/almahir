package com.almahir.iti.repository;

import com.almahir.iti.model.Student;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    @EntityGraph(attributePaths = {"user"})
    @NonNull
    Optional<Student> findById(UUID id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Student> findByUserEmail(String email);

    @EntityGraph(attributePaths = {"user"})
    Page<Student> findByUserFirstNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @NonNull
    Page<Student> findAll(@NonNull Pageable pageable);
    @EntityGraph(attributePaths = {"user"})
    Page<Student> findByUserUsernameContainingIgnoreCase(String username, Pageable pageable);
}
