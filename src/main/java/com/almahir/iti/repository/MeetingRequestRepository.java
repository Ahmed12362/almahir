package com.almahir.iti.repository;

import com.almahir.iti.model.MeetingRequest;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.Student;
import com.almahir.iti.model.enums.MeetingRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<MeetingRequest> findByStatusAndExpiresAtLessThanEqual(
            MeetingRequestStatus status,
            LocalDateTime time
    );

    List<MeetingRequest> findByStudentAndStatus(
            Student student,
            MeetingRequestStatus status,
            Pageable pageable
    );

    Page<MeetingRequest> findByStudentAndStatusOrderByEndedAtDesc(
            Student student,
            MeetingRequestStatus status,
            Pageable pageable
    );

    Page<MeetingRequest> findByStudentAndStatusIn(Student student, List<MeetingRequestStatus> statuses, Pageable pageable);

    Page<MeetingRequest> findBySheikhAndStatusIn(Sheikh sheikh, List<MeetingRequestStatus> statuses, Pageable pageable);
}