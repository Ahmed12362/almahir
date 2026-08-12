package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.CreateSheikhReviewRequest;
import com.almahir.iti.dto.response.SheikhReviewResponse;
import com.almahir.iti.exception.AlreadyExists;
import com.almahir.iti.exception.ForbiddenOperationException;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.SheikhReviewMapper;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.SheikhReview;
import com.almahir.iti.model.Student;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.RoleName;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.SheikhReviewRepository;
import com.almahir.iti.repository.StudentRepository;
import com.almahir.iti.service.SheikhReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SheikhReviewServiceImpl implements SheikhReviewService {

    private final SheikhRepository sheikhRepository;
    private final StudentRepository studentRepository;
    private final SheikhReviewRepository sheikhReviewRepository;
    private final SheikhReviewMapper sheikhReviewMapper;

    @Override
    @Transactional
    public SheikhReviewResponse addReview(User currentUser, UUID sheikhId, CreateSheikhReviewRequest request) {
        if (currentUser.getRoles().stream().noneMatch(role -> role.getName() == RoleName.STUDENT)) {
            throw new ForbiddenOperationException("Only registered students can review a Sheikh.");
        }

        Student student = studentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for user id: " + currentUser.getId()));

        Sheikh sheikh = sheikhRepository.findByIdFetchUser(sheikhId)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with id: " + sheikhId));

        if (sheikhReviewRepository.existsBySheikh_IdAndStudent_Id(sheikhId, student.getId())) {
            throw new AlreadyExists("You already reviewed this Sheikh.");
        }

        String comment = request.comment();
        if (comment == null) {
            comment = "";
        } else if (!StringUtils.hasText(comment)) {
            comment = "";
        }

        SheikhReview review = SheikhReview.builder()
                .sheikh(sheikh)
                .student(student)
                .rate(request.rate())
                .comment(comment)
                .build();

        SheikhReview savedReview = sheikhReviewRepository.save(review);
        updateSheikhAverageRate(sheikh);

        return sheikhReviewMapper.toResponse(savedReview);
    }

    @Override
    public List<SheikhReviewResponse> getReviews(UUID sheikhId) {
        if (!sheikhRepository.existsById(sheikhId)) {
            throw new ResourceNotFoundException("Sheikh not found with id: " + sheikhId);
        }

        return sheikhReviewRepository.findBySheikh_IdOrderByCreatedAtDesc(sheikhId).stream()
                .map(sheikhReviewMapper::toResponse)
                .toList();
    }

    private void updateSheikhAverageRate(Sheikh sheikh) {
        Double averageRate = sheikhReviewRepository.averageRateBySheikhId(sheikh.getId());
        sheikh.setRate(averageRate == null ? 0.0 : averageRate);
        sheikhRepository.save(sheikh);
    }
}
