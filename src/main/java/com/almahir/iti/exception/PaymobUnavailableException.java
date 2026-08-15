package com.almahir.iti.exception;

import lombok.Getter;

@Getter
public class PaymobUnavailableException extends RuntimeException {
    private final String code = "PAYMOB_UNAVAILABLE";

    public PaymobUnavailableException(String message) {
        super(message);
    }
}