package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(UUID id){

        super("Schedule not found with ID : " + id);
    }

    public ScheduleNotFoundException(String name){

        super("Schedule not found with name : " + name);
    }
}
