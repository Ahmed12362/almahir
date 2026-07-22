package com.almahir.iti.mapper;

import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.model.Sheikh;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SheikhMapper {

    @Mapping(target = "id", source = "sheikh.id")
    @Mapping(target = "username", source = "sheikh.user.username")
    @Mapping(target = "firstName", source = "sheikh.user.firstName")
    @Mapping(target = "lastName", source = "sheikh.user.lastName")
    @Mapping(target = "email", source = "sheikh.user.email")
    @Mapping(target = "phoneNumber", source = "sheikh.user.phoneNumber")
    @Mapping(target = "profilePictureUrl", source = "sheikh.user.profilePictureUrl")
    @Mapping(target = "sheikhStatus", source = "sheikh.sheikhStatus")
    @Mapping(target = "rate", source = "sheikh.rate")
    SheikhResponse toSheikhResponse(Sheikh sheikh);
}
