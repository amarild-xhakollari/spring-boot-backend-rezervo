package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class OfferingNotFoundException extends RuntimeException {
    public OfferingNotFoundException(UUID id){

        super("Offering not found with ID : " + id);
    }

    public OfferingNotFoundException(String name){

        super("Offering not found with name : " + name);
    }
}
