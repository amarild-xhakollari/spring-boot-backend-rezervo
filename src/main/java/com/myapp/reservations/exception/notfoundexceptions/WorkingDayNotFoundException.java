package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class WorkingDayNotFoundException extends RuntimeException {
    public WorkingDayNotFoundException(UUID id){

        super("WorkingDay not found with ID : " + id);
    }

    public WorkingDayNotFoundException(String name){

        super("WorkingDay not found with name : " + name);
    }
}
