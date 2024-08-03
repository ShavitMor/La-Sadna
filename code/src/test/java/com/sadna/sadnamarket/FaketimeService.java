package com.sadna.sadnamarket;

import com.sadna.sadnamarket.domain.users.NotificationDTO;
import com.sadna.sadnamarket.service.RealtimeService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FaketimeService extends RealtimeService {
    Map<String, List<String>> messages;

    public FaketimeService() {
        this.messages = new HashMap<>();
    }

    @Override
    public void sendNotification(String username, NotificationDTO notification) {
        if(!messages.containsKey(username)){
            messages.put(username, new LinkedList<>());
        }
        messages.get(username).add(notification.getMessage());
    }

    public List<String> getMessages(String username){
        return messages.get(username);
    }
}
