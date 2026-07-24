package com.almahir.iti.mapper;

import com.almahir.iti.dto.request.CircleCreateRequest;
import com.almahir.iti.dto.response.CircleMemberResponse;
import com.almahir.iti.dto.response.CircleResponse;
import com.almahir.iti.model.Circle;
import com.almahir.iti.model.CircleMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CircleMapper {
    @Mapping(target = "id", source = "circle.sheikh.id")
    @Mapping(target = "memberCount", source = "memberCount")
    CircleResponse toResponse(Circle circle, long memberCount);

    @Mapping(target = "id", source = "membership.user.id")
    @Mapping(target = "username", source = "membership.user.username")
    @Mapping(target = "firstName", source = "membership.user.firstName")
    @Mapping(target = "lastName", source = "membership.user.lastName")
    @Mapping(target = "joinedAt", source = "membership.joinedAt")
    CircleMemberResponse toMemberResponse(CircleMembership membership);
}
