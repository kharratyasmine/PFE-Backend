package com.workpilot.controller;

import com.workpilot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestNotifController {

    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NotificationService notificationService;

    public TestNotifController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    @MessageMapping("/notifications")
        @SendTo("/topic/notifications")
        public String handleNotification(String message) {
            // Vous pouvez ajouter une logique supplémentaire ici
            return message;
        }
    }

