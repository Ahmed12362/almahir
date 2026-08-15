package com.almahir.iti.service;

import com.almahir.iti.dto.request.CreateIntentionRequest;
import com.almahir.iti.dto.response.CreateIntentionResponse;
import com.almahir.iti.dto.response.PaymentStatusResponse;
import com.almahir.iti.model.User;

import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    CreateIntentionResponse createIntention(User user, CreateIntentionRequest request);

    PaymentStatusResponse getStatus(User user, String intentionId);

    void handlePaymobWebhook(Map<String, Object> payload, String hmac);
}