package com.almahir.iti.exception;

public class AlreadyExists extends RuntimeException{
    public AlreadyExists(String message){
        super(message+ " Already exists");
    }
}
