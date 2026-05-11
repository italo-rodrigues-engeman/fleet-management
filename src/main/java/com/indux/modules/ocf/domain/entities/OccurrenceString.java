package com.indux.modules.ocf.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "occurrence_strings")
public class OccurrenceString {
    @Id
    private String id;
    
    private String value;
    
    private String type; // "PERTINENTE" ou "NAO_PERTINENTE"
    
    private String description;
    
    private UUID createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private boolean active;
}

