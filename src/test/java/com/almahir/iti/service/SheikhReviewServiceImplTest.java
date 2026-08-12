package com.almahir.iti.service;

import com.almahir.iti.dto.request.CreateSheikhReviewRequest;
import com.almahir.iti.dto.response.SheikhReviewResponse;
import com.almahir.iti.exception.AlreadyExists;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.mapper.SheikhReviewMapper;
import com.almahir.iti.model.Role;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.SheikhReview;
import com.almahir.iti.model.Student;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.model.enums.Gender;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.SheikhReviewRepository;
import com.almahir.iti.repository.StudentRepository;
import com.almahir.iti.service.impl.SheikhReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheikhReviewServiceImplTest {

    @Mock
    private SheikhRepository sheikhRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SheikhReviewRepository sheikhReviewRepository;

    @Mock
    private SheikhReviewMapper sheikhReviewMapper;

    @InjectMocks
    private SheikhReviewServiceImpl reviewService;

    private UUID sheikhId;
    private UUID studentId;
    private User studentUser;
    private Student student;
    private Sheikh sheikh;

    @BeforeEach
    void setUp() {
        sheikhId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        studentUser = User.builder()
                .id(studentId)
                .username("student1")
                .firstName("Ali")
                .lastName("Ahmed")
                .gender(Gender.MALE)
                .email("student@example.com")
                .roles(Set.of(Role.builder().name(RoleName.STUDENT).build()))
                .build();

        student = Student.builder()
                .id(studentId)
                .user(studentUser)
                .build();

        sheikh = Sheikh.builder()
                .id(sheikhId)
                .user(User.builder().id(sheikhId).username("sheikh1").email("sheikh@example.com").build())
                .rate(0.0)
                .build();
    }

    @Test
    void addReview_ShouldSaveAndUpdateAverage() {
        CreateSheikhReviewRequest request = new CreateSheikhReviewRequest(5, "");
        SheikhReview savedReview = SheikhReview.builder()
                .id(UUID.randomUUID())
                .sheikh(sheikh)
                .student(student)
                .rate(5)
                .comment("")
                .build();
        SheikhReviewResponse response = new SheikhReviewResponse(
                savedReview.getId(),
                sheikhId,
                studentId,
                "student1",
                5,
                "",
                savedReview.getCreatedAt()
        );

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sheikhRepository.findByIdFetchUser(sheikhId)).thenReturn(Optional.of(sheikh));
        when(sheikhReviewRepository.existsBySheikh_IdAndStudent_Id(sheikhId, studentId)).thenReturn(false);
        when(sheikhReviewRepository.save(any(SheikhReview.class))).thenReturn(savedReview);
        when(sheikhReviewRepository.averageRateBySheikhId(sheikhId)).thenReturn(5.0);
        when(sheikhReviewMapper.toResponse(savedReview)).thenReturn(response);

        SheikhReviewResponse result = reviewService.addReview(studentUser, sheikhId, request);

        assertEquals(5, result.rate());
        verify(sheikhRepository).save(sheikh);
        verify(sheikhReviewRepository).save(any(SheikhReview.class));
    }

    @Test
    void addReview_ShouldRejectSecondReviewFromSameStudent() {
        CreateSheikhReviewRequest request = new CreateSheikhReviewRequest(4, "Nice");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sheikhRepository.findByIdFetchUser(sheikhId)).thenReturn(Optional.of(sheikh));
        when(sheikhReviewRepository.existsBySheikh_IdAndStudent_Id(sheikhId, studentId)).thenReturn(true);

        assertThrows(AlreadyExists.class, () -> reviewService.addReview(studentUser, sheikhId, request));
        verify(sheikhReviewRepository, never()).save(any(SheikhReview.class));
    }

    @Test
    void addReview_ShouldRejectNonStudent() {
        User nonStudent = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .roles(Set.of(Role.builder().name(RoleName.SHEIKH).build()))
                .build();

        assertThrows(ForbiddenOperationException.class,
                () -> reviewService.addReview(nonStudent, sheikhId, new CreateSheikhReviewRequest(3, null)));
    }

    @Test
    void getReviews_ShouldReturnList() {
        SheikhReview review = SheikhReview.builder()
                .id(UUID.randomUUID())
                .sheikh(sheikh)
                .student(student)
                .rate(4)
                .comment("Good")
                .build();
        SheikhReviewResponse response = new SheikhReviewResponse(review.getId(), sheikhId, studentId, "student1", 4, "Good", review.getCreatedAt());

        when(sheikhRepository.existsById(sheikhId)).thenReturn(true);
        when(sheikhReviewRepository.findBySheikh_IdOrderByCreatedAtDesc(sheikhId)).thenReturn(List.of(review));
        when(sheikhReviewMapper.toResponse(review)).thenReturn(response);

        var result = reviewService.getReviews(sheikhId);

        assertEquals(1, result.size());
        assertEquals("Good", result.get(0).comment());
    }
}
