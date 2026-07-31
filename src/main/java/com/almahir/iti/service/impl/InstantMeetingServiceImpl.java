package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.SheikhAvailabilityRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.exception.ConflictException;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.MeetingRequestMapper;
import com.almahir.iti.model.MeetingRequest;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.Student;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.MeetingRequestStatus;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.repository.MeetingRequestRepository;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.StudentRepository;
import com.almahir.iti.service.AgoraService;
import com.almahir.iti.service.InstantMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstantMeetingServiceImpl implements InstantMeetingService {

    private final SheikhRepository sheikhRepository;
    private final StudentRepository studentRepository;
    private final MeetingRequestRepository meetingRequestRepository;
    private final AgoraService agoraService;
    private final SimpMessagingTemplate messagingTemplate;
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

        meetingRequest.setStatus(MeetingRequestStatus.ACCEPTED);
        meetingRequest.setAcceptedAt(LocalDateTime.now());
        meetingRequestRepository.save(meetingRequest);

        Sheikh sheikh = meetingRequest.getSheikh();
        sheikh.setSheikhStatus(SheikhStatus.BUSY);
        meetingRequestRepository.findBySheikhAndStatus(sheikh, MeetingRequestStatus.PENDING, Pageable.unpaged())
                .forEach(other -> {
                    if (!other.getId().equals(requestId)) {
                        other.setStatus(MeetingRequestStatus.DECLINED);
                        meetingRequestRepository.save(other);
                        messagingTemplate.convertAndSend("/topic/meeting-requests/" + other.getId(),
                                new StompEventPayload<>("REQUEST_DECLINED", "Sheikh accepted another request"));
                    }
                });
        sheikhRepository.save(sheikh);

        String sheikhToken = agoraService.generateToken(meetingRequest.getChannelName(), sheikh.getId());

        String studentToken = agoraService.generateToken(meetingRequest.getChannelName(), meetingRequest.getStudent().getId());

        AcceptResponse studentWsResponse = new AcceptResponse(
                MeetingRequestStatus.ACCEPTED,
                meetingRequest.getId(),
                meetingRequest.getChannelName(),
                studentToken,
                meetingRequest.getStudent().getId().toString()
        );

        messagingTemplate.convertAndSend("/topic/meeting-requests/" + requestId,
                new StompEventPayload<>("REQUEST_ACCEPTED", studentWsResponse));

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
        meetingRequestRepository.save(meetingRequest);

        Sheikh sheikh = meetingRequest.getSheikh();
        sheikh.setSheikhStatus(SheikhStatus.AVAILABLE);
        sheikhRepository.save(sheikh);

        messagingTemplate.convertAndSend("/topic/meeting-requests/" + requestId,
                new StompEventPayload<>("MEETING_ENDED", requestId));
    }
}