package com.almahir.iti.mapper;

import com.almahir.iti.dto.response.PendingMeetingRequestResponse;
import com.almahir.iti.dto.response.SheikhMeetingHistoryResponse;
import com.almahir.iti.dto.response.StudentMeetingHistoryResponse;
import com.almahir.iti.model.MeetingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeetingRequestMapper {

    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(meetingRequest.getStudent().getUser().getFirstName() + \" \" + meetingRequest.getStudent().getUser().getLastName())")
    @Mapping(target = "studentEmail", source = "student.user.email")
    PendingMeetingRequestResponse toPendingResponse(MeetingRequest meetingRequest);

    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "sheikhId", source = "sheikh.id")
    @Mapping(target = "sheikhName", expression = "java(meetingRequest.getSheikh().getUser().getFirstName() + \" \" + meetingRequest.getSheikh().getUser().getLastName())")
    StudentMeetingHistoryResponse toStudentHistoryResponse(MeetingRequest meetingRequest);

    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(meetingRequest.getStudent().getUser().getFirstName() + \" \" + meetingRequest.getStudent().getUser().getLastName())")
    SheikhMeetingHistoryResponse toSheikhHistoryResponse(MeetingRequest meetingRequest);

}