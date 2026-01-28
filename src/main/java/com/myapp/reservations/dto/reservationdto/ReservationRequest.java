package com.myapp.reservations.dto.reservationdto;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationRequest(
        @NotNull UUID businessId,
        @NotNull UUID offeringId,
        @NotNull LocalDateTime startTime,
        String notes

) {
}
