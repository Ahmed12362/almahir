package com.almahir.iti.service;

import com.almahir.iti.dto.request.CreateSheikhReviewRequest;
import com.almahir.iti.dto.response.SheikhReviewResponse;
import com.almahir.iti.model.User;

import java.util.List;
import java.util.UUID;

public interface SheikhReviewService {
    SheikhReviewResponse addReview(User currentUser, UUID sheikhId, CreateSheikhReviewRequest request);

    List<SheikhReviewResponse> getReviews(UUID sheikhId);
}
