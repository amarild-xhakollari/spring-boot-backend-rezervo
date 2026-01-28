package com.myapp.reservations.dto.timeoffdto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimeOffRequest(
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        String reason,
        @NotNull UUID scheduleSettingsId
) {
}
