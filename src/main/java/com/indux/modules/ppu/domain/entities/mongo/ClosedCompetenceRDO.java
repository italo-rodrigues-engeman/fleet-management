package com.indux.modules.ppu.domain.entities.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.user.SimpleUser;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "rdos_competencias")
@Builder
public class ClosedCompetenceRDO {
    @Id private String id;
    @JsonProperty("rdosFechadas") private List<String> rdosClosed;
    @JsonProperty("aprovador") private SimpleUser approver;
    @JsonProperty("data") private LocalDate createdAt;
    @JsonProperty("competencia") private String competence;
    @JsonProperty("observacoes") private String observations;
    @JsonProperty("conferidor") private Boolean checked;
    @JsonProperty("ano") private Integer year;
    @JsonProperty("mes") private Month month;
    @JsonProperty("projecto") private Long project;
    @JsonProperty("dataInicio") private LocalDate initialDate;
    @JsonProperty("dataFinal") private LocalDate finalDate;
}

