package com.indux.modules.flash_fuel.domain.entities;

import com.indux.modules.flash_fuel.domain.dtos.ApplicantRequest;
import com.mongodb.lang.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "flash_combustivel_solicitantes")
public class ApplicantFlashFuel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nullable
    private String matricula;
    private String nome;
    private String cpf;
    private String regional;
    private Boolean colaborador;
    private BigDecimal tetoValor;
    private Integer filialID;
    private Integer regionalId;

    public static ApplicantFlashFuel fromDTO(ApplicantRequest dto) {
        return ApplicantFlashFuel.builder()
                .matricula(dto.matricula())
                .nome(dto.nome())
                .cpf(dto.cpf())
                .regional(dto.regional())
                .regionalId(dto.regionalId())
                .colaborador(dto.colaborador())
                .tetoValor(dto.tetoValor() != null ? new BigDecimal(dto.tetoValor()) : null)
                .filialID(dto.filialID())
                .build();
    }

}
