package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

public class BookingLeadTimeException extends BusinessRuleViolationException {
    public BookingLeadTimeException(int minLeadTimeHours){

        super("This booking is too short-notice. Minimum lead time is " + minLeadTimeHours + " hours");
    }

    public BookingLeadTimeException(String message){

        super(message);
    }
}
