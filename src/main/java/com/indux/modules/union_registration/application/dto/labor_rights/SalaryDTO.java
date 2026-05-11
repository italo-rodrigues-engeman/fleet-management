package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryDTO {
    
    // 4 - Salários (campos gerais - mantidos para compatibilidade)
    private String pisoSalarial;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate dataBase;
    private String porcentagemReajuste;
    private String dissidio;
    
    // Lista de salários por função
    private List<SalaryFunctionDTO> funcoes;
}







