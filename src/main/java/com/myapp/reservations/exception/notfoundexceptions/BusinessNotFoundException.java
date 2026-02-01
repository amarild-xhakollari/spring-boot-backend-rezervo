package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class BusinessNotFoundException extends NotFoundException {

    public BusinessNotFoundException(UUID id){

        super("Business not found with ID : " + id);
    }

    public BusinessNotFoundException(String name){

        super("Business not found with name : " + name);
    }

}
