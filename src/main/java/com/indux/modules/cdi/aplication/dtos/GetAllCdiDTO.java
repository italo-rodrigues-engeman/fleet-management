package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.cdi.domain.entities.models.Scope;
import com.indux.modules.cdi.domain.entities.models.Stage;
import com.indux.modules.cdi.domain.entities.models.Status;
import com.indux.modules.cdi.domain.entities.models.Type;

import java.util.Date;

public record GetAllCdiDTO (
        String id,
        long autoIncrementId,
        @JsonProperty("solicitante") ApplicantDTO applicant,
        @JsonProperty("createAt")Date createAt,
        @JsonProperty("abrangencia") Scope scope,
        @JsonProperty("etapa") Stage stage,
        @JsonProperty("status")Status status,
        @JsonProperty("pontos") Integer points,
        @JsonProperty("tipo")Type type,
        @JsonProperty("titulo") String title
        ){
}