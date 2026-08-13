package com.almahir.iti.repository;

import com.almahir.iti.model.SheikhReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SheikhReviewRepository extends JpaRepository<SheikhReview, UUID> {
    boolean existsBySheikh_IdAndStudent_Id(UUID sheikhId, UUID studentId);

    Optional<SheikhReview> findBySheikh_IdAndStudent_Id(UUID sheikhId, UUID studentId);

    List<SheikhReview> findBySheikh_IdOrderByCreatedAtDesc(UUID sheikhId);

    @Query("select coalesce(avg(r.rate), 0) from SheikhReview r where r.sheikh.id = :sheikhId")
    Double averageRateBySheikhId(@Param("sheikhId") UUID sheikhId);
}
