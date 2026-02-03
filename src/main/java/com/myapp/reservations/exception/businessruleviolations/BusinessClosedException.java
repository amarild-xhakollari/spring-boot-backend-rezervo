package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

import java.time.DayOfWeek;

public class BusinessClosedException extends BusinessRuleViolationException {
    public BusinessClosedException(DayOfWeek dayOfWeek){

        super("The business is closed on " + dayOfWeek);
    }

    public BusinessClosedException(String message){

        super(message);
    }
}
