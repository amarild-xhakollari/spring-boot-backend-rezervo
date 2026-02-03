package com.myapp.reservations.exception.conflictexceptions;

import com.myapp.reservations.exception.ConflictException;

import java.time.LocalDateTime;

public class ReservationConflictException extends ConflictException {
    public ReservationConflictException(){

        super("This time slot is already reserved by another customer");
    }

    public ReservationConflictException(LocalDateTime startTime){

        super("This time slot starting at " + startTime + " is already reserved by another customer");
    }

    public ReservationConflictException(String message){

        super(message);
    }
}
