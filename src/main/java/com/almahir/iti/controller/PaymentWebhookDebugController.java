// controller/PaymentWebhookDebugController.java
package com.almahir.iti.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class PaymentWebhookDebugController {

    @PostMapping("/api/payment/webhooks/paymob-debug")
    public ResponseEntity<Void> debugWebhook(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) String hmac
    ) {
        log.info("=== PAYMOB WEBHOOK DEBUG ===");
        log.info("hmac query param: {}", hmac);
        log.info("raw body: {}", body);
        return ResponseEntity.ok().build();
    }
}