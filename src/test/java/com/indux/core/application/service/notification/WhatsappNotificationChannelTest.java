package com.indux.core.application.service.notification;

import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.infra.notifcation.whatsapp.WhatsappSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WhatsappNotificationChannelTest {

    @Mock
    private WhatsappSender whatsappSender;

    @InjectMocks
    private WhatsappNotificationChannel whatsappNotificationChannel;

    @Test
    @DisplayName("Should process FIRST_ACCESS template formatting phone number and calling sendWelcomeMessage")
    void testNotifyFirstAccess() {
        Notification notification = new Notification();
        notification.setTemplateName(TokenEventType.Values.FIRST_ACCESS.name());
        notification.setMessage("Welcome Message");
        notification.setVariables(Map.of("expiration", "2025-12-31"));
        
        SimpleUser to = SimpleUser.builder()
                .telefone("11999999999")
                .nome("John Doe")
                .build();
        notification.setTo(to);

        whatsappNotificationChannel.notify(notification);

        verify(whatsappSender).sendWelcomeMessage("5511999999999", "John Doe", "Welcome Message", "2025-12-31");
    }

    @Test
    @DisplayName("Should process FORGET_PASSWORD template formatting phone number and calling sendForgetPassword")
    void testNotifyForgetPassword() {
        Notification notification = new Notification();
        notification.setTemplateName(TokenEventType.Values.FORGET_PASSWORD.name());
        notification.setMessage("Reset Message");
        notification.setVariables(Map.of("expiration", "2025-12-31"));
        
        SimpleUser to = SimpleUser.builder()
                .telefone("11999999999")
                .nome("John Doe")
                .build();
        notification.setTo(to);

        whatsappNotificationChannel.notify(notification);

        verify(whatsappSender).sendForgetPassword("5511999999999", "Reset Message", "2025-12-31");
    }

    @Test
    @DisplayName("Should process generic template formatting phone number and message, calling sendMessage")
    void testNotifyGenericTemplate() {
        Notification notification = new Notification();
        notification.setTemplateName("UNKNOWN_TEMPLATE");
        notification.setSubject("Alert");
        notification.setMessage("System Alert");
        
        SimpleUser to = SimpleUser.builder()
                .telefone("11999999999")
                .build();
        notification.setTo(to);

        whatsappNotificationChannel.notify(notification);

        verify(whatsappSender).sendMessage("5511999999999", "Alert\n\nSystem Alert");
    }

    @Test
    @DisplayName("getType should return WHATSAPP")
    void testGetType() {
        NotificationType type = whatsappNotificationChannel.getType();
        assertEquals(NotificationType.WHATSAPP, type);
    }
}
