package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleUpdateRequest;
import com.almahir.iti.dto.response.CircleMemberResponse;
import com.almahir.iti.dto.response.CircleResponse;
import com.almahir.iti.exception.ConflictException;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.CircleMapper;
import com.almahir.iti.model.Circle;
import com.almahir.iti.model.CircleMembership;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.CircleStatus;
import com.almahir.iti.model.enums.MembershipStatus;
import com.almahir.iti.repository.CircleMembershipRepository;
import com.almahir.iti.repository.CircleRepository;
import com.almahir.iti.service.CircleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public CircleResponse createCircle(User currentUser, CircleCreateRequest request) {
        if(!currentUser.getRoles().contains("SHEIKH")) {
            throw new RuntimeException("You are not a sheikh");
        }

        if(request.startDate().isAfter(request.endDate())) {
            throw new RuntimeException("Start date must be before end date");
        }

        Circle circle = Circle.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(CircleStatus.SCHEDULED)
                .sheikh(currentUser)
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

        return circles.map(circle -> circleMapper.toResponse(
                circle,
                circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public CircleResponse getCircle(UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));
        long memberCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        return circleMapper.toResponse(circle, memberCount);
    }

    @Override
    public CircleResponse updateCircle(User currentUser, UUID circleId, CircleUpdateRequest request) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getSheikh().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Sheikh who created this Circle can update it.");
        }

        String newName = request.name() != null ? request.name() : circle.getName();
        LocalDateTime newStart = request.startDate() != null ? request.startDate() : circle.getStartDate();
        LocalDateTime newEnd = request.endDate() != null ? request.endDate() : circle.getEndDate();
        if(newStart.isAfter(newEnd)) {
            throw new RuntimeException("Start date must be before end date");
        }

        circle.setName(newName);
        circle.setStartDate(newStart);
        circle.setEndDate(newEnd);
        circle = circleRepository.save(circle);

        long memberCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        return circleMapper.toResponse(circle, memberCount);
    }

    @Override
    public void cancelCircle(User currentUser, UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getSheikh().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Sheikh who created this Circle can cancel it.");
        }

        circle.setStatus(CircleStatus.CANCELLED);
        circleRepository.save(circle);
    }

    @Override
    public CircleResponse joinCircle(User currentUser, UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (circle.getStatus() == CircleStatus.CANCELLED || circle.getStatus() == CircleStatus.COMPLETED) {
            throw new ConflictException("This Circle is not open for joining.");
        }

        circleMembershipRepository.findByCircleAndUserAndStatus(circle, currentUser, MembershipStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ConflictException("You have already joined this Circle.");
                });

        List<CircleMembership> overlaps = circleMembershipRepository.findOverlappingActiveMemberships(
                currentUser, circle.getStartDate(), circle.getEndDate());

        if (!overlaps.isEmpty()) {
            Circle conflicting = overlaps.get(0).getCircle();
            throw new ConflictException(
                    "This Circle overlaps with '" + conflicting.getName() + "' ("
                            + conflicting.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "-"
                            + conflicting.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            + "), which you already joined. You cannot attend two Circles at the same time."
            );
        }

        CircleMembership membership = CircleMembership.builder()
                .circle(circle)
                .user(currentUser)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        circleMembershipRepository.save(membership);

        long memberCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        return circleMapper.toResponse(circle, memberCount);
    }

    @Override
    public void leaveCircle(User currentUser, UUID circleId) {
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
    public List<CircleMemberResponse> listMembers(UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        return circleMembershipRepository.findByCircleAndStatusOrderByJoinedAtAsc(circle, MembershipStatus.ACTIVE)
                .stream()
                .map(circleMapper::toMemberResponse)
                .toList();
    }

    @Override
    public void removeMember(User currentUser, UUID circleId, UUID targetUserId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getSheikh().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Sheikh who created this Circle can remove members.");
        }

        CircleMembership membership = circleMembershipRepository
                .findByCircleAndUser_IdAndStatus(circle, targetUserId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Circle or member not found."));

        membership.setStatus(MembershipStatus.REMOVED);
        membership.setEndedAt(LocalDateTime.now());
        membership.setRemovedBy(currentUser);
        circleMembershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CircleResponse> listMyCircles(User currentUser, CircleStatus status) {
        return circleMembershipRepository.findByUserAndStatus(currentUser, MembershipStatus.ACTIVE)
                .stream()
                .map(CircleMembership::getCircle)
                .filter(circle -> status == null || circle.getStatus() == status)
                .map(circle -> circleMapper.toResponse(
                        circle,
                        circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)
                ))
                .toList();
    }
}
