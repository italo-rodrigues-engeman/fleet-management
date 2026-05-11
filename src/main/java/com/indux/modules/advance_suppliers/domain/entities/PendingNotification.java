package com.indux.modules.advance_suppliers.domain.entities;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document("adiantamento_de_fornecedores_notificacoes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingNotification {
    @Id
    private String id;
    private String occurrenceId;
    private Integer step;
    private Instant dueDate;
    private Set<String> responsibleUserIds;
    private String type;
    private boolean notified;
    private Long code;
}