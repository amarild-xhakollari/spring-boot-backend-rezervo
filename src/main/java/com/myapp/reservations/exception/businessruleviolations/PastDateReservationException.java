package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

import java.time.LocalDateTime;

public class PastDateReservationException extends BusinessRuleViolationException {
    public PastDateReservationException(){

        super("Cannot create a reservation for a past date");
    }

    public PastDateReservationException(LocalDateTime requestedDate){

        super("Cannot create a reservation for past date : " + requestedDate);
    }
}
