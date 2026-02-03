package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID id){

        super("Notification not found with ID : " + id);
    }

    public NotificationNotFoundException(String name){

        super("Notification not found with name : " + name);
    }
}
