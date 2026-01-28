package com.myapp.reservations.dto.reviewdto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID businessId,
        UUID userId,
        String userName,
        String userAvatar,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
