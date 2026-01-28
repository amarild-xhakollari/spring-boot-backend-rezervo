package com.myapp.reservations.dto.notificationdto;

import com.myapp.reservations.entities.Notification.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        boolean isRead,
        String targetUrl,
        LocalDateTime createdAt
) {
}
