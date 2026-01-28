package com.myapp.reservations.dto.schedulesettingsdto;

import com.myapp.reservations.dto.workingdaydto.WorkingDayResponse;
import com.myapp.reservations.entities.Reservation.ReservationType;

import java.util.List;
import java.util.UUID;

public record ScheduleSettingsResponse(
        UUID id,
        ReservationType reservationType,
        Integer slotDurationValue,
        java.time.temporal.ChronoUnit slotDurationUnit,
        Integer minAdvanceBookingHours,
        Integer maxAdvanceBookingDays,
        Boolean autoConfirmAppointments,
        List<WorkingDayResponse> workingDays
) {}