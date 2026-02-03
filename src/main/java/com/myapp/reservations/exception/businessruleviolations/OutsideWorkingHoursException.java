package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

import java.time.LocalTime;

public class OutsideWorkingHoursException extends BusinessRuleViolationException {
    public OutsideWorkingHoursException(){

        super("Selected time is outside of business working hours");
    }

    public OutsideWorkingHoursException(LocalTime requestedTime, LocalTime openTime, LocalTime closeTime){

        super("Selected time " + requestedTime + " is outside of business working hours (" + openTime + " - " + closeTime + ")");
    }

    public OutsideWorkingHoursException(String message){

        super(message);
    }
}
