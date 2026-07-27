package com.almahir.iti.mapper;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.response.CircleJoinResponse;
import com.almahir.iti.dto.response.CircleMemberResponse;
import com.almahir.iti.dto.response.CircleResponse;
import com.almahir.iti.dto.response.PendingJoinRequestResponse;
import com.almahir.iti.model.Circle;
import com.almahir.iti.model.CircleMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CircleMapper {
    @Mapping(target = "circleId", source = "circle.id")
    @Mapping(target = "title", source = "circle.title")
    @Mapping(target = "startDate", source = "circle.startDate")
    @Mapping(target = "endDate", source = "circle.endDate")
    @Mapping(target = "status", source = "circle.status")
    @Mapping(target = "memberCount", source = "memberCount")
    CircleResponse toResponse(Circle circle, long memberCount);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "joinedAt", source = "joinedAt")
    CircleMemberResponse toMemberResponse(CircleMembership membership);

    @Mapping(target = "circleId", source = "circle.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "requestedAt", source = "joinedAt")
    CircleJoinResponse toJoinResponse(CircleMembership membership);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "requestedAt", source = "joinedAt")
    PendingJoinRequestResponse toPendingResponse(CircleMembership membership);
}
