package com.myapp.reservations.dto.businessdto;

import com.myapp.reservations.entities.businessentity.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BusinessRequest(
        @NotBlank(message = "Business name is required")
        String name,

        @Size(max = 1000, message = "Description is too long")
        String description,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotNull(message = "Business type is required")
        BusinessType businessType,

        String customType
) {}
