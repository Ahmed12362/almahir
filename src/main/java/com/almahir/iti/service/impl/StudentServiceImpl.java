package com.almahir.iti.service.impl;

import com.almahir.iti.dto.response.PageResponse;
import com.almahir.iti.dto.response.StudentResponse;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.StudentMapper;
import com.almahir.iti.repository.StudentRepository;
import com.almahir.iti.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    public PageResponse<StudentResponse> getAllStudents(Pageable pageable) {
        Page<StudentResponse> studentPage = studentRepository.findAll(pageable)
                .map(studentMapper::toResponse);
        return PageResponse.from(studentPage);
    }

    @Override
    public StudentResponse getStudentById(UUID id) {
        return studentRepository.findById(id)
                .map(studentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    public StudentResponse getStudentByEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .map(studentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
    }

    @Override
    public PageResponse<StudentResponse> searchStudentsByName(String name, Pageable pageable) {
        Page<StudentResponse> studentPage = studentRepository
                .findByUserFirstNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCase(name, name, pageable)
                .map(studentMapper::toResponse);
        return PageResponse.from(studentPage);
    }

    @Override
    public PageResponse<StudentResponse> searchStudentsByUsername(String username, Pageable pageable) {
        Page<StudentResponse> studentPage = studentRepository
                .findByUserUsernameContainingIgnoreCase(username, pageable)
                .map(studentMapper::toResponse);
        return PageResponse.from(studentPage);
    }
}