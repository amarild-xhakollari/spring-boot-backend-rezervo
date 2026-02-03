package com.myapp.reservations.exception.businessruleviolations;

import com.myapp.reservations.exception.BusinessRuleViolationException;

import java.time.LocalTime;

public class BreakTimeConflictException extends BusinessRuleViolationException {
    public BreakTimeConflictException(){

        super("Selected time overlaps with a business break");
    }

    public BreakTimeConflictException(LocalTime breakStart, LocalTime breakEnd){

        super("Selected time overlaps with business break (" + breakStart + " - " + breakEnd + ")");
    }
}
