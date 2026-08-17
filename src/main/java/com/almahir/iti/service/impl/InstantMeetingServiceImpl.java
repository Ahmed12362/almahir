package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.SheikhAvailabilityRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.exception.ConflictException;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.InsufficientMinutesException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.MeetingRequestMapper;
import com.almahir.iti.model.*;
import com.almahir.iti.model.enums.MeetingRequestStatus;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.repository.MeetingRequestRepository;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.StudentRepository;
import com.almahir.iti.repository.UserSubscriptionRepository;
import com.almahir.iti.service.AgoraService;
import com.almahir.iti.service.InstantMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstantMeetingServiceImpl implements InstantMeetingService {

    private final SheikhRepository sheikhRepository;
    private final StudentRepository studentRepository;
    private final MeetingRequestRepository meetingRequestRepository;
    private final AgoraService agoraService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final MeetingRequestMapper meetingRequestMapper;

    @Override
    @Transactional
    public SheikhAvailabilityResponse updateSheikhAvailability(User currentUser, SheikhAvailabilityRequest request) {
        Sheikh sheikh = sheikhRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ForbiddenOperationException("Only registered Sheikhs can update availability."));

        sheikh.setSheikhStatus(request.status());
        sheikh = sheikhRepository.save(sheikh);

        return new SheikhAvailabilityResponse(sheikh.getId(), sheikh.getSheikhStatus(), LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public SheikhAvailabilityResponse getSheikhAvailability(UUID sheikhId) {
        Sheikh sheikh = sheikhRepository.findById(sheikhId)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with ID: " + sheikhId));

        return new SheikhAvailabilityResponse(sheikh.getId(), sheikh.getSheikhStatus(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public MeetingRequestResponse createMeetingRequest(User currentUser, UUID sheikhId) {
        Student student = studentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ForbiddenOperationException("Only registered students can request instant meetings."));

        UserSubscription subscription = userSubscriptionRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new InsufficientMinutesException(
                        "You don't have an active subscription. Please subscribe to a package first."));

        if (subscription.getExpiresAt() == null || subscription.getExpiresAt().isBefore(Instant.now())) {
            throw new InsufficientMinutesException("Your subscription has expired. Please renew your package.");
        }

        if (subscription.getMinutesRemaining() == null || subscription.getMinutesRemaining() <= 0) {
            throw new InsufficientMinutesException("You don't have enough meeting minutes remaining.");
        }
        Sheikh sheikh = sheikhRepository.findById(sheikhId)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with ID: " + sheikhId));

        if (sheikh.getSheikhStatus() != SheikhStatus.AVAILABLE) {
            throw new ConflictException("SHEIKH_UNAVAILABLE");
        }

        meetingRequestRepository.findByStudentAndSheikhAndStatus(student, sheikh, MeetingRequestStatus.PENDING)
                .ifPresent(m -> {
                    if (m.getExpiresAt().isBefore(LocalDateTime.now())) {
                        m.setStatus(MeetingRequestStatus.EXPIRED);
                        meetingRequestRepository.save(m);
                    } else {
                        throw new ConflictException("You already have a pending request with this Sheikh.");
                    }
                });

        String channelName = "instant_1to1_" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);

        MeetingRequest meetingRequest = MeetingRequest.builder()
                .student(student)
                .sheikh(sheikh)
                .status(MeetingRequestStatus.PENDING)
                .channelName(channelName)
                .requestedAt(now)
                .expiresAt(expiresAt)
                .build();

        meetingRequest = meetingRequestRepository.save(meetingRequest);

        SheikhMeetingRequestEvent event = new SheikhMeetingRequestEvent(
                meetingRequest.getId(),
                student.getId(),
                currentUser.getEmail(),
                null,
                expiresAt
        );
        messagingTemplate.convertAndSend("/topic/sheikhs/" + sheikhId + "/requests",
                new StompEventPayload<>("SHEIKH_MEETING_REQUEST_RECEIVED", event));

        return new MeetingRequestResponse(meetingRequest.getId(), meetingRequest.getStatus(), channelName, expiresAt);
    }

    @Override
    @Transactional
    public AcceptResponse acceptMeetingRequest(User currentUser, UUID requestId) {
        MeetingRequest meetingRequest = meetingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting request not found."));

        if (!meetingRequest.getSheikh().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the requested Sheikh can accept this meeting.");
        }

        if (meetingRequest.getStatus() != MeetingRequestStatus.PENDING) {
            throw new ConflictException("Meeting request is not in PENDING status.");
        }

        if (meetingRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            meetingRequest.setStatus(MeetingRequestStatus.EXPIRED);
            meetingRequestRepository.save(meetingRequest);
            throw new ConflictException("Meeting request has expired.");
        }

        Student student = meetingRequest.getStudent();

        UserSubscription subscription = userSubscriptionRepository.findByUserId(student.getId())
                .orElseThrow(() -> new InsufficientMinutesException(
                        "Student doesn't have an active subscription."
                ));

        if (subscription.getExpiresAt() == null ||
                subscription.getExpiresAt().isBefore(Instant.now())) {
            throw new InsufficientMinutesException(
                    "Student's subscription has expired."
            );
        }

        Integer minutesRemaining = subscription.getMinutesRemaining();

        if (minutesRemaining == null || minutesRemaining <= 0) {
            throw new InsufficientMinutesException(
                    "Student doesn't have enough meeting minutes remaining."
            );
        }

        meetingRequest.setMaxDurationMinutes(minutesRemaining);
        meetingRequest.setStatus(MeetingRequestStatus.ACCEPTED);
        meetingRequest.setAcceptedAt(LocalDateTime.now());
        meetingRequestRepository.save(meetingRequest);

        Sheikh sheikh = meetingRequest.getSheikh();
        sheikh.setSheikhStatus(SheikhStatus.BUSY);

        meetingRequestRepository.findBySheikhAndStatus(
                        sheikh,
                        MeetingRequestStatus.PENDING,
                        Pageable.unpaged()
                )
                .forEach(other -> {
                    if (!other.getId().equals(requestId)) {
                        other.setStatus(MeetingRequestStatus.DECLINED);
                        meetingRequestRepository.save(other);

                        messagingTemplate.convertAndSend(
                                "/topic/meeting-requests/" + other.getId(),
                                new StompEventPayload<>(
                                        "REQUEST_DECLINED",
                                        "Sheikh accepted another request"
                                )
                        );

                        messagingTemplate.convertAndSend(
                                "/topic/sheikhs/" + sheikh.getId() + "/requests",
                                new StompEventPayload<>(
                                        "SHEIKH_MEETING_REQUEST_REMOVED",
                                        other.getId()
                                )
                        );
                    }
                });

        meetingRequestRepository.findByStudentAndStatus(
                        student,
                        MeetingRequestStatus.PENDING,
                        Pageable.unpaged()
                )
                .forEach(other -> {
                    if (!other.getId().equals(requestId)) {
                        other.setStatus(MeetingRequestStatus.CANCELLED);
                        meetingRequestRepository.save(other);

                        messagingTemplate.convertAndSend(
                                "/topic/meeting-requests/" + other.getId(),
                                new StompEventPayload<>(
                                        "REQUEST_CANCELLED",
                                        "Student joined another meeting"
                                )
                        );

                        messagingTemplate.convertAndSend(
                                "/topic/sheikhs/" + other.getSheikh().getId() + "/requests",
                                new StompEventPayload<>(
                                        "SHEIKH_MEETING_REQUEST_REMOVED",
                                        other.getId()
                                )
                        );
                    }
                });

        sheikhRepository.save(sheikh);

        String sheikhToken = agoraService.generateToken(
                meetingRequest.getChannelName(),
                sheikh.getId()
        );

        String studentToken = agoraService.generateToken(
                meetingRequest.getChannelName(),
                meetingRequest.getStudent().getId()
        );

        AcceptResponse studentWsResponse = new AcceptResponse(
                MeetingRequestStatus.ACCEPTED,
                meetingRequest.getId(),
                meetingRequest.getChannelName(),
                studentToken,
                meetingRequest.getStudent().getId().toString()
        );

        messagingTemplate.convertAndSend(
                "/topic/meeting-requests/" + requestId,
                new StompEventPayload<>("REQUEST_ACCEPTED", studentWsResponse)
        );

        return new AcceptResponse(
                MeetingRequestStatus.ACCEPTED,
                meetingRequest.getId(),
                meetingRequest.getChannelName(),
                sheikhToken,
                sheikh.getId().toString()
        );
    }

    @Override
    @Transactional
    public void declineMeetingRequest(User currentUser, UUID requestId) {
        MeetingRequest meetingRequest = meetingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting request not found."));

        if (!meetingRequest.getSheikh().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the requested Sheikh can decline this meeting.");
        }

        if (meetingRequest.getStatus() != MeetingRequestStatus.PENDING) {
            throw new ConflictException("Meeting request is not in PENDING status.");
        }

        meetingRequest.setStatus(MeetingRequestStatus.DECLINED);
        meetingRequestRepository.save(meetingRequest);

        messagingTemplate.convertAndSend("/topic/meeting-requests/" + requestId,
                new StompEventPayload<>("REQUEST_DECLINED", "Declined by sheikh"));
    }

    @Override
    @Transactional
    public void cancelMeetingRequest(User currentUser, UUID requestId) {

        MeetingRequest meetingRequest = meetingRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Meeting request not found."));

        if (!meetingRequest.getStudent().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException(
                    "Only the requesting student can cancel this meeting.");
        }

        if (meetingRequest.getStatus() != MeetingRequestStatus.PENDING) {
            throw new ConflictException(
                    "Only a pending request can be cancelled.");
        }

        meetingRequest.setStatus(MeetingRequestStatus.CANCELLED);
        meetingRequestRepository.save(meetingRequest);

        // Notify the student (if still listening)
        messagingTemplate.convertAndSend(
                "/topic/meeting-requests/" + requestId,
                new StompEventPayload<>(
                        "REQUEST_CANCELLED",
                        requestId
                )
        );

        // Notify the sheikh to remove this request from the pending list
        messagingTemplate.convertAndSend(
                "/topic/sheikhs/" + meetingRequest.getSheikh().getId() + "/requests",
                new StompEventPayload<>(
                        "SHEIKH_MEETING_REQUEST_REMOVED",
                        requestId
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AgoraTokenResponse getMeetingToken(User currentUser, UUID requestId) {
        MeetingRequest meetingRequest = meetingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting request not found."));

        boolean isSheikh = meetingRequest.getSheikh().getId().equals(currentUser.getId());
        boolean isStudent = meetingRequest.getStudent().getId().equals(currentUser.getId());

        if (!isSheikh && !isStudent) {
            throw new ForbiddenOperationException("You are not part of this meeting session.");
        }

        if (meetingRequest.getStatus() != MeetingRequestStatus.ACCEPTED) {
            throw new ConflictException("Meeting is not accepted yet.");
        }

        String token = agoraService.generateToken(meetingRequest.getChannelName(), currentUser.getId());
        return new AgoraTokenResponse(token, meetingRequest.getChannelName(), currentUser.getId().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingMeetingRequestResponse> getPendingRequests(User currentUser, Pageable pageable) {
        Sheikh sheikh = sheikhRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ForbiddenOperationException("Only registered Sheikhs can view pending requests."));

        Page<MeetingRequest> pending = meetingRequestRepository
                .findBySheikhAndStatus(sheikh, MeetingRequestStatus.PENDING, pageable);

        return PageResponse.from(pending.map(meetingRequestMapper::toPendingResponse));
    }

    @Transactional
    @Override
    public void expirePendingRequests() {

        List<MeetingRequest> expiredRequests =
                meetingRequestRepository.findByStatusAndExpiresAtLessThanEqual(
                        MeetingRequestStatus.PENDING,
                        LocalDateTime.now()
                );

        if (expiredRequests.isEmpty()) {
            return;
        }

        for (MeetingRequest request : expiredRequests) {

            request.setStatus(MeetingRequestStatus.EXPIRED);

            messagingTemplate.convertAndSend(
                    "/topic/meeting-requests/" + request.getId(),
                    new StompEventPayload<>(
                            "REQUEST_EXPIRED",
                            request.getId()
                    )
            );

            messagingTemplate.convertAndSend(
                    "/topic/sheikhs/" + request.getSheikh().getId() + "/requests",
                    new StompEventPayload<>(
                            "SHEIKH_MEETING_REQUEST_REMOVED",
                            request.getId()
                    )
            );
        }

        meetingRequestRepository.saveAll(expiredRequests);
    }

    @Transactional
    @Override
    public void endMeeting(User currentUser, UUID requestId) {
        MeetingRequest meetingRequest = meetingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting request not found."));

        boolean isSheikh = meetingRequest.getSheikh().getId().equals(currentUser.getId());
        boolean isStudent = meetingRequest.getStudent().getId().equals(currentUser.getId());

        if (!isSheikh && !isStudent) {
            throw new ForbiddenOperationException("You are not part of this meeting session.");
        }

        if (meetingRequest.getStatus() != MeetingRequestStatus.ACCEPTED) {
            throw new ConflictException("Meeting is not currently active.");
        }

        meetingRequest.setEndedAt(LocalDateTime.now());
        meetingRequest.setStatus(MeetingRequestStatus.ENDED);
        meetingRequestRepository.save(meetingRequest);

        deductMeetingMinutes(meetingRequest);

        Sheikh sheikh = meetingRequest.getSheikh();
        sheikh.setSheikhStatus(SheikhStatus.AVAILABLE);
        sheikhRepository.save(sheikh);

        messagingTemplate.convertAndSend("/topic/meeting-requests/" + requestId,
                new StompEventPayload<>("MEETING_ENDED", requestId));
    }

    private static final List<MeetingRequestStatus> DEFAULT_HISTORY_STATUSES =
            List.of(MeetingRequestStatus.ENDED, MeetingRequestStatus.DECLINED,
                    MeetingRequestStatus.CANCELLED, MeetingRequestStatus.EXPIRED);


    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentMeetingHistoryResponse> getStudentMeetingHistory(User currentUser, List<MeetingRequestStatus> statuses, Pageable pageable) {
        Student student = studentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ForbiddenOperationException("Only registered students can view meeting history."));

        List<MeetingRequestStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? DEFAULT_HISTORY_STATUSES
                : statuses;

        Page<MeetingRequest> history = meetingRequestRepository
                .findByStudentAndStatusIn(student, effectiveStatuses, pageable);

        return PageResponse.from(history.map(meetingRequestMapper::toStudentHistoryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SheikhMeetingHistoryResponse> getSheikhMeetingHistory(User currentUser, List<MeetingRequestStatus> statuses, Pageable pageable) {
        Sheikh sheikh = sheikhRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ForbiddenOperationException("Only registered Sheikhs can view meeting history."));

        List<MeetingRequestStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? DEFAULT_HISTORY_STATUSES
                : statuses;

        Page<MeetingRequest> history = meetingRequestRepository
                .findBySheikhAndStatusIn(sheikh, effectiveStatuses, pageable);

        return PageResponse.from(history.map(meetingRequestMapper::toSheikhHistoryResponse));
    }

    @Override
    @Transactional
    public void expireActiveMeetings() {

        List<MeetingRequest> activeMeetings =
                meetingRequestRepository.findByStatusAndAcceptedAtIsNotNull(
                        MeetingRequestStatus.ACCEPTED
                );

        LocalDateTime now = LocalDateTime.now();

        for (MeetingRequest meeting : activeMeetings) {

            if (meeting.getMaxDurationMinutes() == null) {
                continue;
            }

            LocalDateTime maxEndTime = meeting.getAcceptedAt()
                    .plusMinutes(meeting.getMaxDurationMinutes());

            if (now.isBefore(maxEndTime)) {
                continue;
            }

            // Re-check status before ending
            if (meeting.getStatus() != MeetingRequestStatus.ACCEPTED) {
                continue;
            }

            meeting.setEndedAt(maxEndTime);
            meeting.setStatus(MeetingRequestStatus.ENDED);

            deductMeetingMinutes(meeting);

            Sheikh sheikh = meeting.getSheikh();
            sheikh.setSheikhStatus(SheikhStatus.AVAILABLE);

            sheikhRepository.save(sheikh);

            messagingTemplate.convertAndSend(
                    "/topic/meeting-requests/" + meeting.getId(),
                    new StompEventPayload<>(
                            "MEETING_ENDED",
                            meeting.getId()
                    )
            );
        }
        meetingRequestRepository.saveAll(activeMeetings);

    }

    private void deductMeetingMinutes(MeetingRequest meetingRequest) {
        if (meetingRequest.getAcceptedAt() == null || meetingRequest.getEndedAt() == null) {
            return;
        }
        long seconds = Duration.between(meetingRequest.getAcceptedAt(), meetingRequest.getEndedAt()).getSeconds();
        int consumedMinutes = (int) Math.ceil(seconds / 60.0);
        if (consumedMinutes <= 0) {
            return;
        }

        userSubscriptionRepository.findByUserId(meetingRequest.getStudent().getId())
                .ifPresent(sub -> {
                    int remaining = sub.getMinutesRemaining() == null ? 0 : sub.getMinutesRemaining();
                    sub.setMinutesRemaining(Math.max(0, remaining - consumedMinutes));
                    userSubscriptionRepository.save(sub);
                });
    }
}