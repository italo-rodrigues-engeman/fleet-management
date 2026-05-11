package com.indux.core.application.service.notification;

import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationChannelTest {

    @Mock
    private CustomMailSender mailSender;

    @InjectMocks
    private EmailNotificationChannel emailNotificationChannel;

    @Test
    @DisplayName("Should process FIRST_ACCESS template calling sendWelcomeEmail via CustomMailSender")
    void testNotifyFirstAccess() {
        Notification notification = new Notification();
        notification.setTemplateName(TokenEventType.Values.FIRST_ACCESS.name());
        notification.setMessage("Welcome Message");
        notification.setVariables(Map.of("expiration", "2025-12-31"));
        
        SimpleUser to = SimpleUser.builder()
                .email("test@email.com")
                .nome("John Doe")
                .build();
        notification.setTo(to);

        emailNotificationChannel.notify(notification);

        verify(mailSender).sendWelcomeEmail("test@email.com", "John Doe", "Welcome Message", "2025-12-31");
    }

    @Test
    @DisplayName("Should process FORGET_PASSWORD template calling sendPasswordResetEmail via CustomMailSender")
    void testNotifyForgetPassword() {
        Notification notification = new Notification();
        notification.setTemplateName(TokenEventType.Values.FORGET_PASSWORD.name());
        notification.setMessage("Reset Message");
        notification.setVariables(Map.of("expiration", "2025-12-31"));
        
        SimpleUser to = SimpleUser.builder()
                .email("test@email.com")
                .nome("John Doe")
                .build();
        notification.setTo(to);

        emailNotificationChannel.notify(notification);

        verify(mailSender).sendPasswordResetEmail("test@email.com", "John Doe", "Reset Message", "2025-12-31");
    }

    @Test
    @DisplayName("Should send generic email if template name is unknown or missing")
    void testNotifyGenericTemplate() {
        Notification notification = new Notification();
        notification.setTemplateName("UNKNOWN_TEMPLATE");
        notification.setSubject("Subject");
        notification.setMessage("Generic Message");
        notification.setAttachments(Collections.emptyMap());
        
        SimpleUser to = SimpleUser.builder()
                .email("default@email.com")
                .build();
        notification.setTo(to);

        emailNotificationChannel.notify(notification);

        verify(mailSender).sendGenericEmail("default@email.com", "Subject", "Generic Message", Collections.emptyMap());
    }

    @Test
    @DisplayName("getType should return EMAIL")
    void testGetType() {
        NotificationType type = emailNotificationChannel.getType();
        assertEquals(NotificationType.EMAIL, type);
    }
}
