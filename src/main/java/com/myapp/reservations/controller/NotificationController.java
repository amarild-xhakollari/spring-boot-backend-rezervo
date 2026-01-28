package com.myapp.reservations.controller;

import com.myapp.reservations.dto.notificationdto.NotificationResponse;
import com.myapp.reservations.dto.notificationdto.UnreadCountResponse;
import com.myapp.reservations.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        return notificationService.getLatestNotifications();
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount() {
        return new UnreadCountResponse(notificationService.getUnreadCount());
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable UUID id) {
        return notificationService.markAsRead(id);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
