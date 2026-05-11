package com.indux.core.domain.service.notification;

import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;

public interface NotificationChannel {
    /**
     * Envia uma notificação através deste canal.
     * @param notification objeto contendo os dados da notificação
     */
    void notify(Notification notification);

    /**
     * Retorna o tipo de notificação suportado por este canal.
     * @return tipo de notificação definido em NotificationType(EMAIL, SMS, PUSH, etc)
     */
    NotificationType getType();
}
