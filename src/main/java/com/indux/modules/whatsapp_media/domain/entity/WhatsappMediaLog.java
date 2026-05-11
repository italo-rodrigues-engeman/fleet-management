package com.indux.modules.whatsapp_media.domain.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("logs_whatsapp_media")
public class WhatsappMediaLog {
    @Id
    private String id;
    private String createdBy;
    @CreatedDate
    private LocalDate createdAt;
    private String path;
}
