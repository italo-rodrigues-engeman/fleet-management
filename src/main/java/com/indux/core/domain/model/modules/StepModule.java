package com.indux.core.domain.model.modules;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "tb_modulos_etapas")
@NoArgsConstructor
@AllArgsConstructor
public class StepModule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private int etapa;
    private int tempo;
    private String nome, descricao;
    private UUID moduloId;

}
