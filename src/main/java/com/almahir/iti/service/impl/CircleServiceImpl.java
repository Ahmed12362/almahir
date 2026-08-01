package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.*;
import com.almahir.iti.exception.ConflictException;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.CircleMapper;
import com.almahir.iti.model.Circle;
import com.almahir.iti.model.CircleMembership;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.MembershipStatus;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.CircleMembershipRepository;
import com.almahir.iti.repository.CircleRepository;
import com.almahir.iti.service.CircleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CircleServiceImpl implements CircleService {
    private final CircleRepository circleRepository;
    private final CircleMembershipRepository circleMembershipRepository;
    private final CircleMapper circleMapper;

    public CircleServiceImpl(CircleRepository circleRepository,
                             CircleMembershipRepository circleMembershipRepository,
                             CircleMapper circleMapper) {
        this.circleRepository = circleRepository;
        this.circleMapper = circleMapper;
        this.circleMembershipRepository = circleMembershipRepository;
    }

    @Override
    @Transactional
    public CircleResponse createCircle(User currentUser, CircleCreateRequest request) {
        if (currentUser.getRoles().stream().noneMatch(role -> role.getName() == RoleName.SHEIKH)) {
            throw new ForbiddenOperationException("Only registered Sheikhs can create a Circle.");
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new ConflictException("Start date must be before end date");
        }

        Circle circle = Circle.builder()
                .title(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(CircleStatus.SCHEDULED)
                .owner(currentUser)
                .build();

        circle = circleRepository.save(circle);

        return circleMapper.toResponse(circle, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> listCircles(CircleStatus status, Pageable pageable) {
        Page<Circle> circles = (status != null)
                ? circleRepository.findByStatus(status, pageable)
                : circleRepository.findAll(pageable);

        return circles.map(circle -> {
            long count = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
            return circleMapper.toResponse(circle, count);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getMyCircles(User currentUser, Pageable pageable) {
        return circleMembershipRepository.findByUserAndStatus(currentUser, MembershipStatus.ACTIVE, pageable)
                .map(cm -> {
                    long count = circleMembershipRepository.countByCircleAndStatus(cm.getCircle(), MembershipStatus.ACTIVE);
                    return circleMapper.toResponse(cm.getCircle(), count);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public CircleResponse getCircleById(UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));
        long memberCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        return circleMapper.toResponse(circle, memberCount);
    }

    @Override
    @Transactional
    public CircleResponse updateCircle(User currentUser, UUID circleId, CircleUpdateRequest request) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can update it.");
        }

        String newTitle = request.name() != null ? request.name() : circle.getTitle();
        LocalDateTime newStart = request.startDate() != null ? request.startDate() : circle.getStartDate();
        LocalDateTime newEnd = request.endDate() != null ? request.endDate() : circle.getEndDate();
        if (newStart.isAfter(newEnd)) {
            throw new ConflictException("Start date must be before end date");
        }

        circle.setTitle(newTitle);
        circle.setStartDate(newStart);
        circle.setEndDate(newEnd);
        circle = circleRepository.save(circle);

        long memberCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        return circleMapper.toResponse(circle, memberCount);
    }

    @Override
    @Transactional
    public void cancelCircle(User currentUser, UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can cancel it.");
        }

        circle.setStatus(CircleStatus.CANCELLED);
        circleRepository.save(circle);
    }

    @Override
    @Transactional
    public CircleEndResponse endCircle(UUID circleId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can end it.");
        }

        if (circle.getStatus() == CircleStatus.CANCELLED) {
            throw new ConflictException("Cannot end a cancelled circle");
        }

        if (circle.getStatus() == CircleStatus.COMPLETED) {
            throw new ConflictException("Circle is already ended");
        }

        circle.setStatus(CircleStatus.COMPLETED);
        LocalDateTime endedAt = LocalDateTime.now();
        circleRepository.save(circle);

        return new CircleEndResponse(CircleStatus.COMPLETED, endedAt);
    }

    @Override
    @Transactional
    public CircleJoinResponse joinCircle(UUID circleId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (circle.getStatus() == CircleStatus.CANCELLED || circle.getStatus() == CircleStatus.COMPLETED) {
            throw new ConflictException("Cannot join a cancelled or completed circle");
        }

        circleMembershipRepository.findByCircleAndUser(circle, currentUser).ifPresent(m -> {
            if (m.getStatus() == MembershipStatus.ACTIVE) {
                throw new ConflictException("You are already an active member of this circle");
            }
            if (m.getStatus() == MembershipStatus.PENDING) {
                throw new ConflictException("Your join request is already pending approval");
            }
        });

        List<CircleMembership> overlaps = circleMembershipRepository.findOverlappingActiveMemberships(
                currentUser, circle.getStartDate(), circle.getEndDate()
        );

        if (!overlaps.isEmpty()) {
            Circle conflicting = overlaps.getFirst().getCircle();
            throw new ConflictException(String.format(
                    "This circle overlaps with '%s' (%s - %s) which you are already attending.",
                    conflicting.getTitle(), conflicting.getStartDate(), conflicting.getEndDate()
            ));
        }

        CircleMembership membership = circleMembershipRepository.findByCircleAndUser(circle, currentUser)
                .orElseGet(() -> CircleMembership.builder()
                        .circle(circle)
                        .user(currentUser)
                        .build());

        membership.setStatus(MembershipStatus.PENDING);
        membership.setJoinedAt(LocalDateTime.now());
        membership.setRemovedAt(null);
        membership.setRemovedBy(null);

        CircleMembership saved = circleMembershipRepository.save(membership);
        return circleMapper.toJoinResponse(saved);
    }

    @Override
    @Transactional
    public CircleMemberResponse approveJoinRequest(UUID circleId, UUID userId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can approve join requests");
        }

        CircleMembership membership = circleMembershipRepository.findByCircleAndUser(circle, User.builder().id(userId).build())
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found for user: " + userId));

        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ConflictException("Request is not in PENDING state");
        }

        List<CircleMembership> overlaps = circleMembershipRepository.findOverlappingActiveMemberships(
                membership.getUser(), circle.getStartDate(), circle.getEndDate()
        );

        if (!overlaps.isEmpty()) {
            throw new ConflictException("Cannot approve request: user has an overlapping active circle");
        }

        membership.setStatus(MembershipStatus.ACTIVE);
        circleMembershipRepository.save(membership);
        return circleMapper.toMemberResponse(membership);
    }

    @Override
    @Transactional
    public void rejectJoinRequest(UUID circleId, UUID userId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can reject join requests");
        }

        CircleMembership membership = circleMembershipRepository.findByCircleAndUser(circle, User.builder().id(userId).build())
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found for user: " + userId));

        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ConflictException("Request is not in PENDING state");
        }

        membership.setStatus(MembershipStatus.REJECTED);
        circleMembershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PendingJoinRequestResponse> getPendingRequests(UUID circleId, User currentUser, Pageable pageable) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can view pending requests");
        }

        return circleMembershipRepository.findByCircleAndStatus(circle, MembershipStatus.PENDING, pageable)
                .map(circleMapper::toPendingResponse);
    }

    @Override
    @Transactional
    public void leaveCircle(UUID circleId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        CircleMembership membership = circleMembershipRepository
                .findByCircleAndUserAndStatus(circle, currentUser, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this Circle."));

        membership.setStatus(MembershipStatus.LEFT);
        membership.setEndedAt(LocalDateTime.now());
        circleMembershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleMemberResponse> getCircleMembers(UUID circleId, Pageable pageable) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        return circleMembershipRepository.findByCircleAndStatusOrderByJoinedAtAsc(circle, MembershipStatus.ACTIVE, pageable)
                .map(circleMapper::toMemberResponse);
    }

    @Override
    @Transactional
    public void removeMember(UUID circleId, User currentUser, UUID targetUserId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can remove members.");
        }

        CircleMembership membership = circleMembershipRepository
                .findByCircleAndUser_IdAndStatus(circle, targetUserId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Circle or member not found."));

        membership.setStatus(MembershipStatus.REMOVED);
        membership.setEndedAt(LocalDateTime.now());
        membership.setRemovedBy(currentUser);
        circleMembershipRepository.save(membership);
    }
}