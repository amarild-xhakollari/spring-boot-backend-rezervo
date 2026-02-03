package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

import java.time.LocalDateTime;

public class BusinessUnavailableException extends BusinessRuleViolationException {
    public BusinessUnavailableException(){

        super("The business is unavailable during this time (Maintenance/Time Off)");
    }

    public BusinessUnavailableException(LocalDateTime startTime, LocalDateTime endTime, String reason){

        super("The business is unavailable from " + startTime + " to " + endTime + " (" + reason + ")");
    }
}
