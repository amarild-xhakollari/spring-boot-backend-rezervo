package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

public class BookingWindowException extends BusinessRuleViolationException {
    public BookingWindowException(int maxAdvanceDays){

        super("This date is too far in the future. You can only book up to " + maxAdvanceDays + " days in advance");
    }

    public BookingWindowException(String message){

        super(message);
    }
}
