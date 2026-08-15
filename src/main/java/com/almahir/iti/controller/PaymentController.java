package com.almahir.iti.controller;

import com.almahir.iti.dto.request.CreateIntentionRequest;
import com.almahir.iti.dto.response.CreateIntentionResponse;
import com.almahir.iti.dto.response.PaymentStatusResponse;
import com.almahir.iti.model.User;
import com.almahir.iti.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intentions")
    public ResponseEntity<CreateIntentionResponse> createIntention(
            @AuthenticationPrincipal User user,
            @RequestBody CreateIntentionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createIntention(user, request));
    }

    @GetMapping("/intentions/{id}/status")
    public ResponseEntity<PaymentStatusResponse> getStatus(
            @AuthenticationPrincipal User user,
            @PathVariable("id") String intentionId
    ) {
        return ResponseEntity.ok(paymentService.getStatus(user, intentionId));
    }

    @PostMapping("/webhooks/paymob")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) String hmac
    ) {
        paymentService.handlePaymobWebhook(payload, hmac);
        return ResponseEntity.ok().build();
    }
}