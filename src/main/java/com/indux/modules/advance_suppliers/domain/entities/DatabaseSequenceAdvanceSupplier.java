package com.indux.modules.advance_suppliers.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sequence_adiantamento_fornecedores")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequenceAdvanceSupplier {
    @Id
    @SequenceGenerator(
            name = "seqAdiantamento",
            sequenceName = "tb_sequence_adiantamento_fornecedores_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seqAdiantamento"
    )
    private Long id;
    @Column(name = "document_id")
    private String documentId;
}
