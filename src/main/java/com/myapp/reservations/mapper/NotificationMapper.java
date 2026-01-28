package com.myapp.reservations.mapper;

import com.myapp.reservations.dto.notificationdto.NotificationResponse;
import com.myapp.reservations.entities.Notification.Notification;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getTargetUrl(),
                notification.getCreatedAt()
        );
    }
}
