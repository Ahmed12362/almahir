package com.almahir.iti.service;

import com.almahir.iti.dto.request.CreateIntentionRequest;
import com.almahir.iti.dto.response.CreateIntentionResponse;
import com.almahir.iti.dto.response.PaymentStatusResponse;
import com.almahir.iti.dto.response.SubscriptionPackageMeetingMinutesAllowedResponse;
import com.almahir.iti.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    CreateIntentionResponse createIntention(User user, CreateIntentionRequest request);

    PaymentStatusResponse getStatus(User user, String intentionId);

    @Transactional(readOnly = true)
    List<SubscriptionPackageMeetingMinutesAllowedResponse> listActivePackages();

    void handlePaymobWebhook(Map<String, Object> payload, String hmac);
}