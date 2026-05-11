package com.indux.modules.ppu.domain.entities.rdo.logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RDOSyncLog {
    @JsonProperty("rdoId")
    private String rdoId;
    
    @JsonProperty("dataInicial")
    private LocalDateTime syncStartDate;
    
    @JsonProperty("dataFinal")
    private LocalDateTime syncEndDate;
    
    @JsonProperty("sucesso")
    private Boolean success;
    
    @JsonProperty("totalAtualizados")
    private Integer totalUpdated;
    
    @JsonProperty("totalErros")
    private Integer totalErrors;
    
    @JsonProperty("colaboradoresAtualizados")
    private List<EmployeeStatusUpdate> employeesUpdated;
    
    @JsonProperty("erros")
    private List<String> errors;
    
    @JsonProperty("usuario")
    private RDOLoggerUser user;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployeeStatusUpdate {
        @JsonProperty("matricula")
        private String registration;
        
        @JsonProperty("nome")
        private String name;
        
        @JsonProperty("statusAnterior")
        private String previousStatus;
        
        @JsonProperty("statusNovo")
        private String newStatus;
        
        @JsonProperty("atualizado")
        private Boolean updated;
    }
}


