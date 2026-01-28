package com.myapp.reservations.dto.timeoffdto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimeOffResponse(
        UUID id,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String reason
) {
}
