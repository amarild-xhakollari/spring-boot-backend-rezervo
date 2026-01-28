package com.myapp.reservations.dto.timeoffdto.offeringdto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OfferingRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        @Positive Double price,
        @Min(5) Integer durationMinutes,
        @Min(0) Integer bufferTimeMinutes,
        @NotBlank UUID businessId


) {
}
