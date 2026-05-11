package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public record ClassRequest(
        String id,
        String filialHCM,
        @JsonProperty("treinamentoId")
        String trainingId,
        @JsonProperty("instituicaoId")
        String instituinId,
        @JsonProperty("dataInicio")
        LocalDate dateStrart,
        @JsonProperty("dataFim")
        LocalDate dateEnd,
        @JsonProperty("finalizado")
        LocalDate finished,
        @JsonProperty("validade")
        LocalDate validity,
        @JsonProperty("cargaHoraria")
        String workload,
        @JsonProperty("observacao")
        String observation,
        @JsonProperty("tipo")
        String type,
        @JsonProperty("pessoalmente")
        String modality,
        @JsonProperty("capaxidadeMaxima")
        Double capacityMax,
        @JsonProperty("capacidadeMinima")
        Double capacityMin,
        @JsonProperty("colaboradores")
        List<EmployeeRequestDTO>collaborators,
        Boolean status,
        @JsonProperty("anexoCriar")
        MultipartFile file
) {
}
