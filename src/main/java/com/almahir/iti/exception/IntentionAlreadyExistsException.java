package com.almahir.iti.exception;

public class IntentionAlreadyExistsException extends RuntimeException {
    public IntentionAlreadyExistsException(String specialReference) {
        super("Intention with reference '" + specialReference + "' already exists at Paymob.");
    }
}