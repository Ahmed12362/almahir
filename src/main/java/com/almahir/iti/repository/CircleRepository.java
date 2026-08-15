package com.almahir.iti.repository;

import com.almahir.iti.model.Circle;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.CircleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircleRepository extends JpaRepository<Circle, UUID> {
    Page<Circle> findByTypeAndStatus(CircleType type, CircleStatus status, Pageable pageable);

    Page<Circle> findByType(CircleType type, Pageable pageable);

    Page<Circle> findByOwner(User owner, Pageable pageable);

    long countByStatus(CircleStatus status);

    Page<Circle> findByOwner_Id(UUID ownerId, Pageable pageable);

    @Query("select c from Circle c where c.type = com.almahir.iti.model.enums.CircleType.PUBLIC")
    Page<Circle> findAllPublic(Pageable pageable);

    @Query("select c from Circle c where c.type = com.almahir.iti.model.enums.CircleType.PRIVATE")
    Page<Circle> findAllPrivate(Pageable pageable);

    @Query("select c from Circle c where c.owner.id = :sheikhId")
    Page<Circle> findBySheikhId(@Param("sheikhId") UUID sheikhId, Pageable pageable);

    @Query("select distinct cm.circle from CircleMembership cm where cm.user.id = :studentId")
    Page<Circle> findCirclesByStudentId(@Param("studentId") UUID studentId, Pageable pageable);

    Page<Circle> findByOwnerAndType(User owner, CircleType type, Pageable pageable);

    Page<Circle> findByOwnerAndTypeAndStatus(User owner, CircleType type, CircleStatus status, Pageable pageable);

    Optional<Circle> findByInviteToken(String inviteToken);

    boolean existsByInviteToken(String inviteToken);
}