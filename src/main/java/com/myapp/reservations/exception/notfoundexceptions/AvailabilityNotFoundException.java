package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class AvailabilityNotFoundException extends RuntimeException {
    public AvailabilityNotFoundException(UUID id){

        super("Availability not found with ID : " + id);
    }

    public AvailabilityNotFoundException(String name){

        super("Availability not found with name : " + name);
    }
}
