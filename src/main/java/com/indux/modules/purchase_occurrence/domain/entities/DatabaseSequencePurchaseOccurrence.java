package com.indux.modules.purchase_occurrence.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sequence_purchase_occurrence")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequencePurchaseOccurrence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "document_id")
    private String documentId;
}
