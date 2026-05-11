package com.indux.modules.ppu.domain.entities.rdo.logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOJustification;
import com.indux.modules.ppu.domain.entities.item.RDORejectionType;
import com.mongodb.lang.Nullable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder @AllArgsConstructor @NoArgsConstructor @Getter @Setter
public class RDOLogger {
    @JsonProperty("foiAnalisado") private Boolean wasAnalyzed;
    @JsonProperty("dadosCorretos") private Boolean isInfoCorrect;
    @JsonProperty("motivos") private List<RDORejectionType> type;
    @JsonProperty("acao") private RDOLoggerType action;
    @JsonProperty("justificativa") @Nullable private String justification;
    @JsonProperty("justificativaCoordenador") @Nullable private CreateRDOJustification coordinatorJustification;
    @JsonProperty("usuario") private RDOLoggerUser user;
    @JsonProperty("data") private LocalDateTime date;
    @JsonProperty("setor") private String sector;
    @JsonProperty("colaboradoresAtualizados") @Nullable private List<RDOSyncLog.EmployeeStatusUpdate> colaboradoresAtualizados;
}

