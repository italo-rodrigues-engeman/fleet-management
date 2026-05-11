package com.indux.core.application.service.notification;

import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.notification.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationService {
    private final Map<NotificationType, NotificationChannel> channels;

    public NotificationService(List<NotificationChannel> implementations) {
        this.channels = implementations.stream()
                .collect(Collectors.toMap(NotificationChannel::getType, ch -> ch));
    }

    public void send(Notification notification) {
        NotificationChannel channel = channels.get(notification.getType());
        if (channel != null) {
            channel.notify(notification);
        } else {
            throw new IllegalArgumentException("Canal de notificação não implementado: " + notification.getType());
        }
    }

    public void sendToAll(Notification notification, Set<NotificationType> types) {
        types.forEach(type -> {
            NotificationChannel channel = channels.get(type);
            if (channel != null) {
                channel.notify(notification);
            }
        });
    }
}

