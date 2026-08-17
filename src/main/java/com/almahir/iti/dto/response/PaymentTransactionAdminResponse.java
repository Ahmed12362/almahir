package com.almahir.iti.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PaymentTransactionAdminResponse(
        UUID transactionId,
        UUID userId,
        String userFullName,
        String userEmail,
        String packageCode,
        String packageName,
        String method,
        String status,
        Long amountMinorUnits,
        String currencyCode,
        String paymobIntentionId,
        String paymobTransactionId,
        String failureReasonCode,
        Instant createdAt,
        Instant updatedAt
) {
}