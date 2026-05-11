package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DisciplinaryProceduresDTO {
    
    // 13 - Procedimentos Disciplinares
    private String multasPenalidades;
    private String projecaoAviso;
    private String trintidio; // aplica projeção do aviso sim ou não
    @JsonAlias({"multaEncerramentoTempoServico"})
    private String multaEncerramentoContratoTempoServico;
    @JsonAlias({"multaEncerramentoParada"})
    private String multaEncerramentoContratoParada;
}