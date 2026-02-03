package com.myapp.reservations.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message){

        super(message);
    }
}
