package com.myapp.reservations.exception.notfoundexceptions;

public class OfferingNotFoundException extends RuntimeException {
    public OfferingNotFoundException(String message) {
        super(message);
    }
}
