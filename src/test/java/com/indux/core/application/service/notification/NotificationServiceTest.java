package com.indux.core.application.service.notification;

import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.notification.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationChannel emailChannel;

    @Mock
    private NotificationChannel whatsappChannel;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(emailChannel.getType()).thenReturn(NotificationType.EMAIL);
        when(whatsappChannel.getType()).thenReturn(NotificationType.WHATSAPP);

        notificationService = new NotificationService(List.of(emailChannel, whatsappChannel));
    }

    @Test
    @DisplayName("send should find the appropriate channel and call notify")
    void testSendSuccess() {
        Notification notification = new Notification();
        notification.setType(NotificationType.EMAIL);

        notificationService.send(notification);

        verify(emailChannel).notify(notification);
        verify(whatsappChannel, never()).notify(any());
    }

    @Test
    @DisplayName("send should throw IllegalArgumentException when channel is not configured")
    void testSendThrowsExceptionWhenChannelNotFound() {
        NotificationService emptyService = new NotificationService(List.of());

        Notification notification = new Notification();
        notification.setType(NotificationType.EMAIL);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyService.send(notification);
        });

        assertEquals("Canal de notificação não implementado: EMAIL", exception.getMessage());
    }

    @Test
    @DisplayName("sendToAll should invoke notify on each provided type with configured channels")
    void testSendToAllSuccess() {
        Notification notification = new Notification();

        notificationService.sendToAll(notification, Set.of(NotificationType.EMAIL, NotificationType.WHATSAPP));

        verify(emailChannel).notify(notification);
        verify(whatsappChannel).notify(notification);
    }

    @Test
    @DisplayName("sendToAll should safely ignore unknown types without throwing exception")
    void testSendToAllIgnoresMissingChannels() {
        Notification notification = new Notification();

        NotificationService partialService = new NotificationService(List.of(emailChannel));

        partialService.sendToAll(notification, Set.of(NotificationType.EMAIL, NotificationType.WHATSAPP));

        verify(emailChannel).notify(notification);
    }
}
