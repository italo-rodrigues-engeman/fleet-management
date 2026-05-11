package com.indux.modules.flash_fuel.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sequence_flash_combustivel")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequenceFlashFuel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "document_id")
    private String documentId;
}
