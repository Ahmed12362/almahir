package com.almahir.iti.service;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.CircleMemberResponse;
import com.almahir.iti.dto.response.CircleResponse;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CircleService {
    CircleResponse createCircle(User currentUser, CircleCreateRequest request);
    Page<CircleResponse> listCircles(CircleStatus status, Pageable pageable);
    CircleResponse getCircle(UUID circleId);
    CircleResponse updateCircle(User currentUser, UUID circleId, CircleUpdateRequest request);
    void cancelCircle(User currentUser, UUID circleId);
    CircleResponse joinCircle(User currentUser, UUID circleId);
    void leaveCircle(User currentUser, UUID circleId);
    List<CircleMemberResponse> listMembers(UUID circleId);
    void removeMember(User currentUser, UUID circleId, UUID targetUserId);
    List<CircleResponse> listMyCircles(User currentUser, CircleStatus status);
}
