package com.myapp.reservations.service;

import com.myapp.reservations.dto.notificationdto.NotificationRequest;
import com.myapp.reservations.dto.notificationdto.NotificationResponse;
import com.myapp.reservations.mapper.NotificationMapper;
import com.myapp.reservations.repository.NotificationRepository;
import com.myapp.reservations.repository.UserRepository;
import com.myapp.reservations.entities.Notification.Notification;
import com.myapp.reservations.entities.Notification.NotificationType;
import com.myapp.reservations.entities.User.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<NotificationResponse> getLatestNotifications() {
        UUID userId = userService.getCurrentUserId();
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount() {
        UUID userId = userService.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID userId = userService.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to access this notification");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toResponse(saved);
    }

    @Transactional
    public int markAllAsRead() {
        UUID userId = userService.getCurrentUserId();
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .targetUrl(request.targetUrl())
                .build();

        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toResponse(saved);
    }

    @Transactional
    public void createNotificationForUser(UUID userId, String title, String message,
                                          NotificationType type,
                                          String targetUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .build();

        notificationRepository.save(notification);
    }
}
