package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(UUID id){

        super("Reservation not found with ID : " + id);
    }

    public ReservationNotFoundException(String name){

        super("Reservation not found with name : " + name);
    }
}
