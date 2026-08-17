package com.almahir.iti.service;

import com.almahir.iti.dto.response.PageResponse;
import com.almahir.iti.dto.response.StudentResponse;
import com.almahir.iti.dto.response.StudentSubscriptionMinutesResponse;
import com.almahir.iti.model.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentService {
    StudentResponse getStudentById(UUID id);

    StudentResponse getStudentByEmail(String email);

    PageResponse<StudentResponse> getAllStudents(Pageable pageable);

    PageResponse<StudentResponse> searchStudentsByName(String name, Pageable pageable);

    PageResponse<StudentResponse> searchStudentsByUsername(String username, Pageable pageable);

    StudentSubscriptionMinutesResponse getSubscriptionMinutes(User currentUser);
}

