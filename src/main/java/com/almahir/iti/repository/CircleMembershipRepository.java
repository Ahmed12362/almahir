package com.almahir.iti.repository;

import com.almahir.iti.model.Circle;
import com.almahir.iti.model.CircleMembership;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircleMembershipRepository extends JpaRepository<CircleMembership, UUID> {
    Optional<CircleMembership> findByCircleAndUserAndStatus(Circle circle, User user, MembershipStatus status);

    Page<CircleMembership> findByCircleAndStatusOrderByJoinedAtAsc(Circle circle, MembershipStatus status, Pageable pageable);

    Page<CircleMembership> findByUserAndStatus(User user, MembershipStatus status, Pageable pageable);

    Optional<CircleMembership> findByCircleAndUser_IdAndStatus(Circle circle, UUID userId, MembershipStatus status);

    Optional<CircleMembership> findByCircleAndUser(Circle circle, User user);

    Page<CircleMembership> findByCircleAndStatus(Circle circle, MembershipStatus status, Pageable pageable);

    long countByCircleAndStatus(Circle circle, MembershipStatus status);

    @Query("""
            SELECT c.id, COUNT(cm)
            FROM CircleMembership cm
            JOIN cm.circle c
            WHERE cm.circle IN :circles AND cm.status = :status
            GROUP BY c.id
            """)
    List<Object[]> countActiveMembersGroupedByCircle(
            @Param("circles") List<Circle> circles,
            @Param("status") MembershipStatus status
    );

    @Query("""
            SELECT cm FROM CircleMembership cm
            JOIN cm.circle c
            WHERE cm.user = :user
              AND cm.status = com.almahir.iti.model.enums.MembershipStatus.ACTIVE
              AND c.startDate < :endDate
              AND c.endDate > :startDate
            """)
    List<CircleMembership> findOverlappingActiveMemberships(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Page<CircleMembership> findByUserAndStatusIn(User user, List<MembershipStatus> statuses, Pageable pageable);
}
