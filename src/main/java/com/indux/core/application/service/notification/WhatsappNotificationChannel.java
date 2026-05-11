package com.indux.core.application.service.notification;

import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.notification.NotificationChannel;
import com.indux.core.infra.notifcation.whatsapp.WhatsappSender;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class WhatsappNotificationChannel implements NotificationChannel {
    private final WhatsappSender sender;
    private final Map<String, Consumer<Notification>> templateHandlers = new HashMap<>();

    public WhatsappNotificationChannel(WhatsappSender sender) {
        this.sender = sender;

        templateHandlers.put(TokenEventType.Values.FIRST_ACCESS.name(), this::handleFirstAccess);
        templateHandlers.put(TokenEventType.Values.FORGET_PASSWORD.name(), this::handleForgetPassword);
    }

    @Override
    public void notify(Notification notification) {
        String type = notification.getTemplateName();

        Consumer<Notification> handler = templateHandlers.get(type);
        if (handler != null) {
            handler.accept(notification);
        } else {
            // Enviar mensagem genérica via WhatsApp
            String phone = "55" + notification.getTo().getTelefone();
            String message = notification.getSubject() + "\n\n" + notification.getMessage();
            
            
            sender.sendMessage(phone, message);
        }
    }

    private void handleFirstAccess(Notification n) {
        sender.sendWelcomeMessage(
                "55" + n.getTo().getTelefone(),
                n.getTo().getNome(),
                n.getMessage(),
                n.getVariables().get("expiration").toString()
        );
    }

    private void handleForgetPassword(Notification n) {
        sender.sendForgetPassword(
                "55" + n.getTo().getTelefone(),
                n.getMessage(),
                n.getVariables().get("expiration").toString()
        );
    }

    @Override
    public NotificationType getType() {
        return NotificationType.WHATSAPP;
    }
}
