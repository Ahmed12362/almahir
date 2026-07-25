package com.almahir.iti.controller;

import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.PageResponse;
import com.almahir.iti.dto.response.StudentResponse;
import com.almahir.iti.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Endpoints for retrieving student profiles and directory search")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @Operation(
            summary = "Get all students",
            description = "Retrieves a paginated list of all registered students."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Students retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Valid JWT token required",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> getAllStudents(
            @ParameterObject
            @PageableDefault(size = 10, sort = "user.firstName") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Students retrieved successfully",
                studentService.getAllStudents(pageable)
        ));
    }

    @Operation(
            summary = "Get student by ID",
            description = "Fetches detailed information of a specific student using their UUID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student details retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found with the given ID",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(
            @Parameter(description = "UUID of the student", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Student retrieved successfully",
                studentService.getStudentById(id)
        ));
    }

    @Operation(
            summary = "Get student by email",
            description = "Fetches a student profile matching the provided email address."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No student found with this email",
                    content = @Content
            )
    })
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByEmail(
            @Parameter(description = "Email address of the student", example = "student@almahir.com")
            @RequestParam String email
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Student retrieved successfully",
                studentService.getStudentByEmail(email)
        ));
    }

    @Operation(
            summary = "Search students by name",
            description = "Searches for students matching the given query string against their first or last name."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> searchStudentsByName(
            @Parameter(description = "Name search query (matches partial first or last name)", example = "Ahmed")
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 10, sort = "user.firstName") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Students retrieved successfully",
                studentService.searchStudentsByName(name, pageable)
        ));

    }

    @Operation(
            summary = "Search students by username",
            description = "Searches for students matching the given query string against their username."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping("/search/username")
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> searchStudentsByUsername(
            @Parameter(description = "Username search query", example = "ahmed_ramadan")
            @RequestParam String username,
            @ParameterObject
            @PageableDefault(size = 10, sort = "user.username") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Students retrieved successfully",
                studentService.searchStudentsByUsername(username, pageable)
        ));
    }
}