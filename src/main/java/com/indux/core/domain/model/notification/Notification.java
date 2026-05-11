package com.indux.core.domain.model.notification;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.model.modules.form.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Notification {
    private SimpleUser to;
    private String subject;
    private String templateName; //Whatsapp and Email templates
    private String message;
    private Map<String, Object> variables; //Se necessário em templates
    private NotificationType type;
    private Map<String, FileMetadata> attachments; //Para email e whatsapp
}
