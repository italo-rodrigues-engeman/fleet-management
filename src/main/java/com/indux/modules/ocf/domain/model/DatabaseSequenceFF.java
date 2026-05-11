package com.indux.modules.ocf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sequence_ff")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequenceFF {
    @Id
    @SequenceGenerator(
            name = "seqFF",
            sequenceName = "tb_sequence_ff_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seqFF"
    )
    private Long id;
    @Column(name = "document_id")
    private String documentId;
}
