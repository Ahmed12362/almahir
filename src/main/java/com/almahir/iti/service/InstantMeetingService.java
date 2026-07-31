package com.almahir.iti.service;

import com.almahir.iti.dto.request.SheikhAvailabilityRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface InstantMeetingService {

    SheikhAvailabilityResponse updateSheikhAvailability(User currentUser, SheikhAvailabilityRequest request);

    SheikhAvailabilityResponse getSheikhAvailability(UUID sheikhId);

    MeetingRequestResponse createMeetingRequest(User currentUser, UUID sheikhId);

    AcceptResponse acceptMeetingRequest(User currentUser, UUID requestId);

    void declineMeetingRequest(User currentUser, UUID requestId);

    void cancelMeetingRequest(User currentUser, UUID requestId);

    AgoraTokenResponse getMeetingToken(User currentUser, UUID requestId);

    PageResponse<PendingMeetingRequestResponse> getPendingRequests(User currentUser, Pageable pageable);

    @Transactional
    void endMeeting(User currentUser, UUID requestId);
}