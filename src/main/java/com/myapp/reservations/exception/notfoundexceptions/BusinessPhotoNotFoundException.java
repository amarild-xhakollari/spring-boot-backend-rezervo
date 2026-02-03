package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class BusinessPhotoNotFoundException extends RuntimeException {
    public BusinessPhotoNotFoundException(UUID id){

        super("BusinessPhoto not found with ID : " + id);
    }

    public BusinessPhotoNotFoundException(String name){

        super("BusinessPhoto not found with name : " + name);
    }
}
