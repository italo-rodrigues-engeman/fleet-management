package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.cdi.domain.entities.models.*;

import java.util.Date;

public record GetIdCdiDTO(

        @JsonProperty("id") String id,

        long autoIncrementId,

        @JsonProperty("solicitante") ApplicantDTO applicant,

        @JsonProperty("tipo") Type type,

        @JsonProperty("escopo") Scope scope,

        @JsonProperty("complexidade") Complexity complexity,

        @JsonProperty("tempoPrevisto") PrevistTime previstTime,

        @JsonProperty("descricao") String description,

        @JsonProperty("independente") Boolean independent,

        @JsonProperty("detalhes") DetailsDTO details,

        @JsonProperty("problema") String problem,

        @JsonProperty("resultado") String result,

        @JsonProperty("resultadoEspecifico") EspecificResultDTO especificResult,

        @JsonProperty("status") Status status,

        @JsonProperty("etapa") Stage stage,

        @JsonProperty("local") AvaliationDTO local,

        @JsonProperty("avaliador1") AvaliationDTO dir1,

        @JsonProperty("avaliador2") AvaliationDTO dir2,

        @JsonProperty("criadoAt") Date createAt,

        @JsonProperty("pontos") Integer points,

        @JsonProperty("prioridade") Complexity priortyLevel,

        @JsonProperty("responsavel") ApplicantDTO finalResponsabilty,

        @JsonProperty("titulo") String title

) {
}
