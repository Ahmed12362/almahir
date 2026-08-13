package com.almahir.iti.mapper;

import com.almahir.iti.dto.response.SheikhReviewResponse;
import com.almahir.iti.model.SheikhReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SheikhReviewMapper {

    @Mapping(target = "sheikhId", source = "sheikh.id")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentUsername", source = "student.user.username")
    SheikhReviewResponse toResponse(SheikhReview review);
}
