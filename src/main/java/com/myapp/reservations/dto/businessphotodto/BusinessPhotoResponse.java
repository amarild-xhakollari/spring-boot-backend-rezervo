package com.myapp.reservations.dto.businessphotodto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BusinessPhotoResponse(
        UUID id,
        String url,
        String caption,
        Integer displayOrder,
        LocalDateTime createdAt
) {}
