package com.almahir.iti.repository;

import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.enums.SheikhStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SheikhRepository extends JpaRepository<Sheikh, UUID> {

    @Query("""
            select s
            from Sheikh s
            join fetch s.user u
            where lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', :name, '%'))
            """)
    List<Sheikh> findByFullNameContainingIgnoreCase(@Param("name") String name);

    @Query("select s from Sheikh s join fetch s.user")
    List<Sheikh> findAllWithUser();

    @Query("select s from Sheikh s join fetch s.user u where s.id = :id")
    Optional<Sheikh> findByIdFetchUser(@Param("id") UUID id);

    @Query("select s from Sheikh s join fetch s.user u where lower(u.email) = lower(:email)")
    Optional<Sheikh> findByUserEmailFetchUser(@Param("email") String email);

    @Query("select s from Sheikh s join fetch s.user u where lower(u.username) = lower(:username)")
    Optional<Sheikh> findByUserUsernameFetchUser(@Param("username") String username);

    Page<Sheikh> findBySheikhStatus(SheikhStatus sheikhStatus, Pageable pageable);

}