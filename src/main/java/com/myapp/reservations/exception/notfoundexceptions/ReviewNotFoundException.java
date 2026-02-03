package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(UUID id){

        super("Review not found with ID : " + id);
    }

    public ReviewNotFoundException(String name){

        super("Review not found with name : " + name);
    }
}
