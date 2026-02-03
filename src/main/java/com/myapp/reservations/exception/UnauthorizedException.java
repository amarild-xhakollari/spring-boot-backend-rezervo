package com.myapp.reservations.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String resourceType, String action){

        super("Not authorized to " + action + " this " + resourceType);
    }

    public UnauthorizedException(String message){

        super(message);
    }
}
