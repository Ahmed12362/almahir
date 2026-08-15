package com.almahir.iti.exception;

import lombok.Getter;

@Getter
public class InvalidPackageException extends RuntimeException {
    private final String code = "INVALID_PACKAGE";

    public InvalidPackageException(String message) {
        super(message);
    }
}