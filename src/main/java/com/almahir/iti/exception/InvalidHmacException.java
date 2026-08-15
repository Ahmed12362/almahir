package com.almahir.iti.exception;

public class InvalidHmacException extends RuntimeException {
    public InvalidHmacException(String message) {
        super(message);
    }
}