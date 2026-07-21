package com.almahir.iti.repository;

import com.almahir.iti.model.Sheikh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}