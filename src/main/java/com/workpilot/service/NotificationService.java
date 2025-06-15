package com.workpilot.service;

import com.workpilot.dto.NotificationDTO;
import com.workpilot.entity.Notification;
import com.workpilot.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendNotification(String title, String message, String roleTargeted) {
        logger.info("Attempting to send notification - Title: {}, Message: {}, Role: {}", title, message, roleTargeted);

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRoleTargeted(roleTargeted);

        try {
            notification = notificationRepository.save(notification);
            logger.info("Notification saved to DB with ID: {}", notification.getId());

            String destination = "/topic/notification";
            if ("ADMIN".equalsIgnoreCase(roleTargeted)) {
                destination = "/topic/admin-notifications";
            }

            logger.info("Sending to destination: {}", destination);
            messagingTemplate.convertAndSend(destination, notification);
            logger.info("Notification sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send notification", e);
        }
    }
    public void sendNotificationToUser(String userId, String title, String message, String roleTargeted) {
        // Sauvegarder la notification
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRoleTargeted(roleTargeted);

        logger.info("Sauvegarde de la notification pour l'utilisateur {}: {}", userId, message);
        notificationRepository.save(notification);

        // Envoyer à un utilisateur spécifique
        messagingTemplate.convertAndSendToUser(userId, "/topic/notification", message);
    }
}
