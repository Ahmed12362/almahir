package com.almahir.iti.exception;

import lombok.Getter;

@Getter
public class InsufficientMinutesException extends RuntimeException {
    private final String code;

    public InsufficientMinutesException(String message) {
        super(message);
        this.code = "INSUFFICIENT_MINUTES";
    }
}