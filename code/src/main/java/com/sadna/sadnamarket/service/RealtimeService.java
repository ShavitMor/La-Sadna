package com.sadna.sadnamarket.service;

import com.sadna.sadnamarket.domain.users.Notification;
import com.sadna.sadnamarket.domain.users.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeService {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendNotification(String username, NotificationDTO notification) {
        template.convertAndSend("/topic/notifications/" + username, notification);
    }
}
