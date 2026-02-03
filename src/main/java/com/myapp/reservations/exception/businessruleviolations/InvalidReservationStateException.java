package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

public class InvalidReservationStateException extends BusinessRuleViolationException {
    public InvalidReservationStateException(String currentState, String action){

        super("Cannot " + action + " reservation in " + currentState + " state");
    }

    public InvalidReservationStateException(String message){

        super(message);
    }
}
