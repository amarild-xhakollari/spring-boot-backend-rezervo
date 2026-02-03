package com.myapp.reservations.exception.notfoundexceptions;

import java.util.UUID;

public class ScheduleSettingsNotFoundException extends RuntimeException {
    public ScheduleSettingsNotFoundException(UUID id){

        super("ScheduleSettings not found with ID : " + id);
    }

    public ScheduleSettingsNotFoundException(String name){

        super("ScheduleSettings not found with name : " + name);
    }
}
