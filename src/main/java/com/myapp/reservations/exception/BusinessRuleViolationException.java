package com.myapp.reservations.exception;

public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message){

        super(message);
    }
}
