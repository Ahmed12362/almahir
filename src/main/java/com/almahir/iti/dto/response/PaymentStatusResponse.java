package com.almahir.iti.dto.response;

public record PaymentStatusResponse(
        String status,
        String transactionId,
        String failureReasonCode
) {
}