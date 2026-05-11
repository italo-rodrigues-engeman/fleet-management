package com.indux.modules.union_registration.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_sindicato_seq")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequenceUnion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "documento_id")
    private String documentId;
}
