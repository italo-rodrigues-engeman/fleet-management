package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanoSaudeOdontologicoDTO {
    private BeneficioEstruturadoDTO beneficio;
    
    // Campos específicos do Plano de Saúde/Odontológico
    private String cobertura; // Quarto Coletivo / Individual
    private String abrangencia; // Nacional / Regional
    private String extensividade; // Individual / Extensiva aos Dependentes
    private String idadeCorteDependentes; // Mais que 21 / Mais que 24
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate dataCorte;
}

