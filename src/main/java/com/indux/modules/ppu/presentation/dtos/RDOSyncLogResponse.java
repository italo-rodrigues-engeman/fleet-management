package com.indux.modules.ppu.presentation.dtos;

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
public class RDOSyncLogResponse {
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
    private List<EmployeeStatusUpdateResponse> employeesUpdated;

    @JsonProperty("erros")
    private List<String> errors;

    @JsonProperty("usuario")
    private UserResponse user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployeeStatusUpdateResponse {
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("nome")
        private String name;
    }
}
