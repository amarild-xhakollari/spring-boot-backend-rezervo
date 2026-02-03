package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class TimeOffNotFoundException extends RuntimeException {
    public TimeOffNotFoundException(UUID id){

        super("TimeOff not found with ID : " + id);
    }

    public TimeOffNotFoundException(String name){

        super("TimeOff not found with name : " + name);
    }
}
