package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.request.CircleJoinRequest;
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
import com.almahir.iti.model.enums.CircleType;
import com.almahir.iti.model.enums.MembershipStatus;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.CircleMembershipRepository;
import com.almahir.iti.repository.CircleRepository;
import com.almahir.iti.service.AgoraService;
import com.almahir.iti.service.CircleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircleServiceImpl implements CircleService {

    private final CircleRepository circleRepository;
    private final CircleMembershipRepository circleMembershipRepository;
    private final CircleMapper circleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AgoraService agoraService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public CircleHostResponse createCircle(User currentUser, CircleCreateRequest request) {
        boolean isSheikh = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.SHEIKH);

        if (request.type() == CircleType.PUBLIC && !isSheikh) {
            throw new ForbiddenOperationException("Only registered Sheikhs can create a public Circle.");
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new ConflictException("Start date must be before end date");
        }

        String passwordHash = null;
        String inviteToken = null;
        boolean requiresApproval = request.requiresApproval();

        if (request.type() == CircleType.PRIVATE) {
            if (request.password() == null || request.password().isBlank()) {
                throw new ConflictException("Password is required for a private Circle");
            }
            passwordHash = passwordEncoder.encode(request.password());
//            requiresApproval = false;
            inviteToken = generateUniqueInviteToken();
        }

        String channelName = "circle_" + UUID.randomUUID().toString().substring(0, 8);

        Circle circle = Circle.builder()
                .title(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(CircleStatus.SCHEDULED)
                .type(request.type())
                .requiresApproval(requiresApproval)
                .maxParticipants(request.maxParticipants())
                .passwordHash(passwordHash)
                .inviteToken(inviteToken)
                .channelName(channelName)
                .owner(currentUser)
                .build();

        circle = circleRepository.save(circle);
        return circleMapper.toHostResponse(circle, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> listCircles(CircleStatus status, Pageable pageable) {
        Page<Circle> circlesPage = (status != null)
                ? circleRepository.findByTypeAndStatus(CircleType.PUBLIC, status, pageable)
                : circleRepository.findByType(CircleType.PUBLIC, pageable);

        Map<UUID, Long> countMap = getActiveMemberCounts(circlesPage.getContent());

        return circlesPage.map(circle ->
                circleMapper.toResponse(circle, countMap.getOrDefault(circle.getId(), 0L))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getMyCircles(User currentUser, Pageable pageable) {
        Page<CircleMembership> memberships = circleMembershipRepository
                .findByUserAndStatus(currentUser, MembershipStatus.ACTIVE, pageable);

        List<Circle> circles = memberships.getContent().stream()
                .map(CircleMembership::getCircle)
                .toList();

        Map<UUID, Long> countMap = getActiveMemberCounts(circles);

        return memberships.map(cm ->
                circleMapper.toResponse(cm.getCircle(), countMap.getOrDefault(cm.getCircle().getId(), 0L))
        );
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
    @Transactional(readOnly = true)
    public Page<CircleResponse> getAllCircles(Pageable pageable) {
        return circleRepository.findAll(pageable)
                .map(circle -> circleMapper.toResponse(circle,
                        circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getPublicCircles(Pageable pageable) {
        return circleRepository.findAllPublic(pageable)
                .map(circle -> circleMapper.toResponse(circle,
                        circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getPrivateCircles(Pageable pageable) {
        return circleRepository.findAllPrivate(pageable)
                .map(circle -> circleMapper.toResponse(circle,
                        circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getCirclesBySheikhId(UUID sheikhId, Pageable pageable) {
        return circleRepository.findBySheikhId(sheikhId, pageable)
                .map(circle -> circleMapper.toResponse(circle,
                        circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getCirclesByStudentId(UUID studentId, Pageable pageable) {
        return circleRepository.findCirclesByStudentId(studentId, pageable)
                .map(circle -> circleMapper.toResponse(circle,
                        circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE)));
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

        if (circle.getStatus() != CircleStatus.SCHEDULED) {
            throw new ConflictException("Only a scheduled circle (not yet started) can be cancelled. Use end for an ongoing circle.");
        }

        circle.setStatus(CircleStatus.CANCELLED);
        circleRepository.save(circle);

        messagingTemplate.convertAndSend("/topic/circles/" + circleId,
                new StompEventPayload<>("CIRCLE_CANCELLED", circleId));
    }

    @Override
    @Transactional
    public CircleEndResponse endCircle(UUID circleId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can end it.");
        }

        if (circle.getStatus() != CircleStatus.ONGOING) {
            throw new ConflictException("Only an ongoing circle can be ended. Use cancel for a circle that hasn't started yet.");
        }

        circle.setStatus(CircleStatus.COMPLETED);
        LocalDateTime endedAt = LocalDateTime.now();
        circleRepository.save(circle);

        messagingTemplate.convertAndSend("/topic/circles/" + circleId,
                new StompEventPayload<>("CIRCLE_ENDED", circleId));

        return new CircleEndResponse(CircleStatus.COMPLETED, endedAt);
    }

    @Override
    @Transactional
    public CircleJoinResponse joinCircle(UUID circleId, User currentUser, CircleJoinRequest request) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (circle.getType() == CircleType.PRIVATE) {
            String providedPassword = request != null ? request.password() : null;
            if (providedPassword == null || providedPassword.isBlank()
                    || !passwordEncoder.matches(providedPassword, circle.getPasswordHash())) {
                throw new ForbiddenOperationException("Invalid or missing password for this Circle.");
            }
        }

        return processJoin(circle, currentUser);
    }

    @Override
    @Transactional
    public CircleJoinResponse joinCircleByToken(String inviteToken, User currentUser) {
        Circle circle = circleRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invite link."));

        return processJoin(circle, currentUser);
    }

    private CircleJoinResponse processJoin(Circle circle, User currentUser) {
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

        MembershipStatus targetStatus = circle.isRequiresApproval()
                ? MembershipStatus.PENDING
                : MembershipStatus.ACTIVE;

        if (targetStatus == MembershipStatus.ACTIVE) {
            assertHasCapacity(circle);
        }

        CircleMembership membership = circleMembershipRepository.findByCircleAndUser(circle, currentUser)
                .orElseGet(() -> CircleMembership.builder()
                        .circle(circle)
                        .user(currentUser)
                        .build());

        membership.setStatus(targetStatus);
        membership.setJoinedAt(LocalDateTime.now());
        membership.setRemovedAt(null);
        membership.setRemovedBy(null);

        CircleMembership saved = circleMembershipRepository.save(membership);

        if (targetStatus == MembershipStatus.PENDING) {
            messagingTemplate.convertAndSend("/topic/circles/" + circle.getId() + "/requests",
                    new StompEventPayload<>("CIRCLE_JOIN_REQUEST_RECEIVED", circleMapper.toPendingResponse(saved)));
        } else {
            messagingTemplate.convertAndSend("/topic/circles/" + circle.getId(),
                    new StompEventPayload<>("MEMBER_JOINED", circleMapper.toMemberResponse(saved)));
        }

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

        assertHasCapacity(circle);

        membership.setStatus(MembershipStatus.ACTIVE);
        CircleMembership saved = circleMembershipRepository.save(membership);

        CircleMemberResponse response = circleMapper.toMemberResponse(saved);

        // Tell the requester directly: they're in
        messagingTemplate.convertAndSend("/topic/circle-memberships/" + saved.getId(),
                new StompEventPayload<>("REQUEST_APPROVED", response));

        // Remove from the owner's pending list view
        messagingTemplate.convertAndSend("/topic/circles/" + circleId + "/requests",
                new StompEventPayload<>("CIRCLE_JOIN_REQUEST_REMOVED", saved.getId()));

        // Tell existing members someone new joined
        messagingTemplate.convertAndSend("/topic/circles/" + circleId,
                new StompEventPayload<>("MEMBER_JOINED", response));

        return response;
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

        messagingTemplate.convertAndSend("/topic/circle-memberships/" + membership.getId(),
                new StompEventPayload<>("REQUEST_REJECTED", "Rejected by circle owner"));

        messagingTemplate.convertAndSend("/topic/circles/" + circleId + "/requests",
                new StompEventPayload<>("CIRCLE_JOIN_REQUEST_REMOVED", membership.getId()));
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

        messagingTemplate.convertAndSend("/topic/circles/" + circleId,
                new StompEventPayload<>("MEMBER_LEFT", currentUser.getId()));
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

        messagingTemplate.convertAndSend("/topic/circles/" + circleId,
                new StompEventPayload<>("MEMBER_REMOVED", targetUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public AgoraTokenResponse getCircleToken(User currentUser, UUID circleId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (circle.getStatus() != CircleStatus.ONGOING) {
            throw new ConflictException("This circle is not currently active. Ask the owner to start it first.");
        }

        boolean isOwner = circle.getOwner().getId().equals(currentUser.getId());
        boolean isActiveMember = isOwner || circleMembershipRepository
                .findByCircleAndUserAndStatus(circle, currentUser, MembershipStatus.ACTIVE)
                .isPresent();

        if (!isActiveMember) {
            throw new ForbiddenOperationException("You are not an active member of this Circle.");
        }

        String token = agoraService.generateToken(circle.getChannelName(), currentUser.getId());
        return new AgoraTokenResponse(token, circle.getChannelName(), currentUser.getId().toString());
    }

    @Override
    @Transactional
    public CircleResponse startCircle(UUID circleId, User currentUser) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (!circle.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only the Circle owner can start it.");
        }

        if (circle.getStatus() != CircleStatus.SCHEDULED) {
            throw new ConflictException("Only a scheduled circle can be started.");
        }

        circle.setStatus(CircleStatus.ONGOING);
        circle = circleRepository.save(circle);

        messagingTemplate.convertAndSend("/topic/circles/" + circleId,
                new StompEventPayload<>("CIRCLE_STARTED", circleId));

        long memberCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        return circleMapper.toResponse(circle, memberCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleMemberResponse> getCircleMembers(UUID circleId, User currentUser, Pageable pageable) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found."));

        if (circle.getType() == CircleType.PRIVATE) {
            boolean isOwner = circle.getOwner().getId().equals(currentUser.getId());
            boolean isActiveMember = isOwner || circleMembershipRepository
                    .findByCircleAndUserAndStatus(circle, currentUser, MembershipStatus.ACTIVE)
                    .isPresent();
            if (!isActiveMember) {
                throw new ForbiddenOperationException("You are not an active member of this Circle.");
            }
        }

        return circleMembershipRepository.findByCircleAndStatusOrderByJoinedAtAsc(circle, MembershipStatus.ACTIVE, pageable)
                .map(circleMapper::toMemberResponse);
    }

    private static final List<MembershipStatus> DEFAULT_HISTORY_STATUSES =
            List.of(MembershipStatus.ACTIVE, MembershipStatus.LEFT, MembershipStatus.REMOVED);

    @Override
    @Transactional(readOnly = true)
    public Page<CircleResponse> getCircleHistory(User currentUser, List<MembershipStatus> statuses, Pageable pageable) {
        List<MembershipStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? DEFAULT_HISTORY_STATUSES
                : statuses;

        Page<CircleMembership> memberships = circleMembershipRepository
                .findByUserAndStatusIn(currentUser, effectiveStatuses, pageable);

        List<Circle> circles = memberships.getContent().stream()
                .map(CircleMembership::getCircle)
                .toList();

        Map<UUID, Long> countMap = getActiveMemberCounts(circles);

        return memberships.map(cm ->
                circleMapper.toResponse(cm.getCircle(), countMap.getOrDefault(cm.getCircle().getId(), 0L))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleHostResponse> getAllPrivateCircleForHost(User currentUser, CircleStatus status, Pageable pageable) {
        Page<Circle> circlesPage = (status != null)
                ? circleRepository.findByOwnerAndTypeAndStatus(currentUser, CircleType.PRIVATE, status, pageable)
                : circleRepository.findByOwnerAndType(currentUser, CircleType.PRIVATE, pageable);

        Map<UUID, Long> countMap = getActiveMemberCounts(circlesPage.getContent());

        return circlesPage.map(circle ->
                circleMapper.toHostResponse(circle, countMap.getOrDefault(circle.getId(), 0L))
        );
    }

    private Map<UUID, Long> getActiveMemberCounts(List<Circle> circles) {
        if (circles.isEmpty()) {
            return Map.of();
        }
        return circleMembershipRepository.countActiveMembersGroupedByCircle(circles, MembershipStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    private void assertHasCapacity(Circle circle) {
        if (circle.getMaxParticipants() == null) {
            return;
        }
        long activeCount = circleMembershipRepository.countByCircleAndStatus(circle, MembershipStatus.ACTIVE);
        if (activeCount >= circle.getMaxParticipants()) {
            throw new ConflictException("This circle has reached its maximum number of participants.");
        }
    }

    private String generateUniqueInviteToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (circleRepository.existsByInviteToken(token));
        return token;
    }
}