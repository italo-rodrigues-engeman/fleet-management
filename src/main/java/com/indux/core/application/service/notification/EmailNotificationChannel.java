package com.indux.core.application.service.notification;

import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.notification.NotificationChannel;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class EmailNotificationChannel implements NotificationChannel {
    private final CustomMailSender mailSender;
    private final Map<String, Consumer<Notification>> templateHandlers = new HashMap<>();

    public EmailNotificationChannel(CustomMailSender mailSender) {
        this.mailSender = mailSender;

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
            mailSender.sendGenericEmail(
                    notification.getTo().getEmail(),
                    notification.getSubject(),
                    notification.getMessage(),
                    notification.getAttachments()
            );
        }
    }

    private void handleFirstAccess(Notification n) {
        mailSender.sendWelcomeEmail(
                n.getTo().getEmail(),
                n.getTo().getNome(),
                n.getMessage(),
                n.getVariables().get("expiration").toString()
        );
    }

    private void handleForgetPassword(Notification n) {
        mailSender.sendPasswordResetEmail(
                n.getTo().getEmail(),
                n.getTo().getNome(),
                n.getMessage(),
                n.getVariables().get("expiration").toString()
        );
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}
