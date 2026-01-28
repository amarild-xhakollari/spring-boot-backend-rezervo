package com.myapp.reservations.mapper;

import com.myapp.reservations.dto.timeoffdto.TimeOffRequest;
import com.myapp.reservations.dto.timeoffdto.TimeOffResponse;
import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import com.myapp.reservations.entities.BusinessSchedule.TimeOff;

public class TimeOffMapper {

    public static TimeOff toTimeOff(TimeOffRequest request, ScheduleSettings settings) {
        if (request == null) {
            return null;
        }

        TimeOff timeOff = new TimeOff();
        timeOff.setStartDateTime(request.startDateTime());
        timeOff.setEndDateTime(request.endDateTime());
        timeOff.setReason(request.reason());

        timeOff.setScheduleSettings(settings);

        return timeOff;
    }

    public static TimeOffResponse toResponse(TimeOff entity) {
        if (entity == null) {
            return null;
        }

        return new TimeOffResponse(
                entity.getId(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getReason()
        );
    }
}