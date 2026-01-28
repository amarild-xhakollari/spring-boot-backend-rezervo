package com.myapp.reservations.dto.businessdto;

import com.myapp.reservations.dto.schedulesettingsdto.ScheduleSettingsResponse;
import com.myapp.reservations.dto.timeoffdto.offeringdto.OfferingResponse;
import com.myapp.reservations.entities.businessentity.BusinessType;

import java.util.List;
import java.util.UUID;

public record BusinessResponse(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        UUID ownerId,
        List<UUID> adminIds,
        ScheduleSettingsResponse schedule,
        List<OfferingResponse> offerings,
        BusinessType businessType,
        String customType,
        String category,
        String imageUrl
) {}
