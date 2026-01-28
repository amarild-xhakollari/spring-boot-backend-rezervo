package com.myapp.reservations.dto.reservationdto;

import com.myapp.reservations.entities.Reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID businessId,
        String businessName,
        UUID offeringId,
        String offeringName,
        UUID userId,
        String userName,

        LocalDateTime startDateTime,
        LocalDateTime endDateTime,

        ReservationStatus status,
        LocalDateTime createdAt
) {}
