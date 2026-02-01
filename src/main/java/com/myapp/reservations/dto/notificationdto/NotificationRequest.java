package com.myapp.reservations.dto.notificationdto;

import com.myapp.reservations.entities.notification.NotificationType;

import java.util.UUID;

public record NotificationRequest(
        UUID userId,
        String title,
        String message,
        NotificationType type,
        String targetUrl
) {
}
