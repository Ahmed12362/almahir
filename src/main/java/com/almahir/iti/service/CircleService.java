package com.almahir.iti.service;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleJoinRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CircleService {
    CircleResponse createCircle(User currentUser, CircleCreateRequest request);

    Page<CircleResponse> listCircles(CircleStatus status, Pageable pageable);

    Page<CircleResponse> getMyCircles(User currentUser, Pageable pageable);

    CircleResponse getCircleById(UUID circleId);

    Page<CircleResponse> getAllCircles(Pageable pageable);

    Page<CircleResponse> getPublicCircles(Pageable pageable);

    Page<CircleResponse> getPrivateCircles(Pageable pageable);

    Page<CircleResponse> getCirclesBySheikhId(UUID sheikhId, Pageable pageable);

    Page<CircleResponse> getCirclesByStudentId(UUID studentId, Pageable pageable);

    CircleResponse updateCircle(User currentUser, UUID circleId, CircleUpdateRequest request);

    void cancelCircle(User currentUser, UUID circleId);

    CircleEndResponse endCircle(UUID circleId, User currentUser);

    CircleJoinResponse joinCircle(UUID circleId, User currentUser, CircleJoinRequest request);

    CircleJoinResponse joinCircleByToken(String inviteToken, User currentUser);

    Page<PendingJoinRequestResponse> getPendingRequests(UUID circleId, User currentUser, Pageable pageable);

    CircleMemberResponse approveJoinRequest(UUID circleId, UUID targetUserId, User currentUser);

    void rejectJoinRequest(UUID circleId, UUID targetUserId, User currentUser);

    void leaveCircle(UUID circleId, User currentUser);

    void removeMember(UUID circleId, User currentUser, UUID targetUserId);

    AgoraTokenResponse getCircleToken(User currentUser, UUID circleId);

    CircleResponse startCircle(UUID circleId, User currentUser);

    Page<CircleMemberResponse> getCircleMembers(UUID circleId, User currentUser, Pageable pageable);

    Page<CircleResponse> getCircleHistory(User currentUser, List<MembershipStatus> statuses, Pageable pageable);

    Page<CircleHostResponse> getAllPrivateCircleForHost(User currentUser, CircleStatus status, Pageable pageable);
}