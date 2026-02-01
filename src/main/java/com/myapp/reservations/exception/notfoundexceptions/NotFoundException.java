package com.myapp.reservations.exception.notfoundexceptions;

public class NotFoundException extends RuntimeException{

    protected NotFoundException(String message){
        super(message);
    }
}
