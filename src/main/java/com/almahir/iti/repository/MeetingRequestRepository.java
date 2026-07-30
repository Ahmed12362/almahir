package com.almahir.iti.repository;

import com.almahir.iti.model.MeetingRequest;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.Student;
import com.almahir.iti.model.enums.MeetingRequestStatus;
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
public interface MeetingRequestRepository extends JpaRepository<MeetingRequest, UUID> {

    Optional<MeetingRequest> findByStudentAndSheikhAndStatus(
            Student student,
            Sheikh sheikh,
            MeetingRequestStatus status
    );

    Optional<MeetingRequest> findByChannelName(String channelName);

    Page<MeetingRequest> findBySheikhAndStatus(
            Sheikh sheikh,
            MeetingRequestStatus status,
            Pageable pageable
    );

    Page<MeetingRequest> findByStudent(Student student, Pageable pageable);

    @Query("""
            SELECT m FROM MeetingRequest m 
            WHERE m.status = com.almahir.iti.model.enums.MeetingRequestStatus.PENDING 
              AND m.expiresAt < :now
            """)
    List<MeetingRequest> findAllExpiredPendingRequests(@Param("now") LocalDateTime now);
}