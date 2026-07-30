package com.almahir.iti.dto.response;


public record StompEventPayload<T>(
        String eventType,
        T payload
) {
}