package com.almahir.iti.mapper;

import com.almahir.iti.dto.response.StudentResponse;
import com.almahir.iti.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phoneNumber", target = "phoneNumber")
    @Mapping(source = "user.profilePictureUrl", target = "profilePictureUrl")
    StudentResponse toResponse(Student student);
}