package com.myapp.reservations.dto.schedulesettingsdto;

import com.myapp.reservations.dto.workingdaydto.WorkingDayRequest;
import com.myapp.reservations.entities.Reservation.ReservationType;
import jakarta.validation.constraints.Min;

import java.util.List;

public record ScheduleSettingsRequest(
        ReservationType reservationType,
        @Min(1) Integer slotDurationValue,
        java.time.temporal.ChronoUnit slotDurationUnit,
        @Min(0) Integer minAdvanceBookingHours,
        @Min(1) Integer maxAdvanceBookingDays,
        Boolean autoConfirmAppointments,
        List<WorkingDayRequest> workingDays
) {}
