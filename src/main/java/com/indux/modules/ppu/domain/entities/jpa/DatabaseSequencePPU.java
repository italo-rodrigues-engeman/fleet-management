package com.indux.modules.ppu.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sequence_ppu")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequencePPU {
    @Id
    @SequenceGenerator(
            name = "seqPPU",
            sequenceName = "tb_sequence_ppu_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seqPPU"
    )
    private Long id;
    @Column(name = "document_id")
    private String documentId;
}

