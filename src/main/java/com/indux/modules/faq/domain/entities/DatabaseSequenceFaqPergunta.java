package com.indux.modules.faq.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sequence_faq_pergunta")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequenceFaqPergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "document_id")
    private String documentId;
}


