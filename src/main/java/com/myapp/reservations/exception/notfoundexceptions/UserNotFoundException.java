package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id){

        super("User not found with ID : " + id);
    }

    public UserNotFoundException(String name){

        super("User not found with name : " + name);
    }
}
