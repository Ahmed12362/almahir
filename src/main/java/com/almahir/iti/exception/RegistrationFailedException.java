package com.almahir.iti.exception;

public class RegistrationFailedException extends RuntimeException {
    public RegistrationFailedException() {
        super("Registration could not be completed. Please try again.");
    }
}
